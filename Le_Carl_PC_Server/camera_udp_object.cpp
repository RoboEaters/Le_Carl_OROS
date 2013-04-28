#include "camera_udp_object.h"
#include <QDebug>
#include <QByteArray>
#include <QPixmap>
#include <QVector>

#define HEADER_SIZE 5;
#define DATAGRAM_MAX_SIZE 1450;
#define DATA_MAX_SIZE 1445;

Camera_UDP_Object::Camera_UDP_Object(QHostAddress ip, quint16 port)
{
    udpSocket = new QUdpSocket(this);
    connect(udpSocket, SIGNAL(readyRead()),this, SLOT(read_image()));

    bool b = udpSocket->bind(ip, port);
    qDebug()<<"udp cam bound: "<< b << " , ip server: "<< ip.toString() << " , port server: "<< port;

    current_frame = -1;
    slicesStored = 0;
    nb_total_frames = 0;
}

void Camera_UDP_Object::close_socket()
{
    udpSocket->close();
    this->deleteLater();
    qDebug()<<"socket close";
}

void Camera_UDP_Object::read_image()
{
    QByteArray datagram;
    datagram.resize(udpSocket->pendingDatagramSize());
    QHostAddress sender;
    quint16 senderPort;

    udpSocket->readDatagram(datagram.data(), datagram.size(),&sender, &senderPort);

    int frame_nb = (int)datagram.at(0);
    int nb_packets = (int)datagram.at(1);
    int packet_nb = (int)datagram.at(2);
    int size_packet = (int) ((datagram.at(3) & 0xff) << 8 | (datagram.at(4) & 0xff));

    if((packet_nb==0) && (current_frame != frame_nb))
    {
        current_frame = frame_nb;
        imageData.clear();
        slicesStored=0;
        size_packets = 0;
        slices.clear();
        slices = QVector<QByteArray>(nb_packets);
    }

    if(frame_nb == current_frame)
    {
        datagram.remove(0,5);
        slicesStored++;
        size_packets += size_packet;

        slices[packet_nb] = datagram;

//        qDebug() << "current frame: " << current_frame << " packet_nb: " << packet_nb
//                 << " slicesStored:" << slicesStored << " nb_packets:" <<nb_packets
//                 << "size packet: " << size_packet << "size data: " << datagram.size();

        if (slicesStored == nb_packets)
        {
            for(int ii=0; ii<slicesStored; ii++)
            {
                imageData.append(slices.at(ii));
            }

            int size_d = imageData.size();
            nb_total_frames++;

            the_frame.loadFromData(imageData);

            emit this->frame_ready(the_frame, size_d);
        }
    }
}
