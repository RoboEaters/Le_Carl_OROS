#include "nn_udp_object.h"
#include <QApplication>
#include <QTime>
#include <QStringList>
#include <QDateTime>

NN_udp_object::NN_udp_object(QHostAddress ip, quint16 port)
{
    udpSocket = new QUdpSocket(this);
    connect(udpSocket, SIGNAL(readyRead()),this, SLOT(read_data()));

    bool b = udpSocket->bind(ip, port);
    qDebug()<<"udp ioio bound: "<< b << " , ip: "<< ip.toString() << " , port: "<< port;

    QDateTime date = QDateTime::currentDateTime();

    path = QString(QApplication::applicationDirPath());
    path.append("/DATA_" + date.toString("MM_dd_hh_mm") + ".txt");
}

void NN_udp_object::close_socket()
{
    udpSocket->close();
    this->deleteLater();
    qDebug()<<"socket close";
}

void NN_udp_object::read_data()
{
    //    while (udpSocket->hasPendingDatagrams())
    //    {
    QByteArray datagram;
    datagram.resize(udpSocket->pendingDatagramSize());
    QHostAddress sender;
    quint16 senderPort;

    udpSocket->readDatagram(datagram.data(), datagram.size(),&sender, &senderPort);
    msg = QString(datagram);
    save_data(msg, path);

    emit values_ready(msg);
    //    }
}

void NN_udp_object::save_data(QString data, QString path)
{
    QFile file_data(path);
    file_data.open(QIODevice::Append | QIODevice::Text);
    QTextStream out(&file_data);
    out << data;
    out.flush();
    file_data.close();
}
