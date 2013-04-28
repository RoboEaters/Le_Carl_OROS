#include "sensors_udp_object.h"

Sensors_UDP_Object::Sensors_UDP_Object(QHostAddress ip, quint16 port)
{
    udpSocket = new QUdpSocket(this);
    connect(udpSocket, SIGNAL(readyRead()),this, SLOT(read_data()));

    bool b = udpSocket->bind(ip, port);
    qDebug()<<"udp sensors bound: "<< b << " , ip: "<< ip.toString() << " , port: "<< port;
}

void Sensors_UDP_Object::close_socket()
{
    udpSocket->close();
    this->deleteLater();
    qDebug()<<"socket close";
}

void Sensors_UDP_Object::read_data()
{
    while (udpSocket->hasPendingDatagrams())
    {
        QByteArray datagram;
        datagram.resize(udpSocket->pendingDatagramSize());
        QHostAddress sender;
        quint16 senderPort;

        udpSocket->readDatagram(datagram.data(), datagram.size(),&sender, &senderPort);

        QString data = QString(datagram);

        emit values_ready(data);
    }
}
