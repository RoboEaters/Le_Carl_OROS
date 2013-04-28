#include "ioio_udp_object.h"

IOIO_UDP_Object::IOIO_UDP_Object(QHostAddress ip, quint16 port)
{
    udpSocket = new QUdpSocket(this);
    connect(udpSocket, SIGNAL(readyRead()),this, SLOT(read_data()));

    bool b = udpSocket->bind(ip, port);
    qDebug()<<"udp ioio bound: "<< b << " , ip: "<< ip.toString() << " , port: "<< port;
}

void IOIO_UDP_Object::close_socket()
{
    udpSocket->close();
    this->deleteLater();
    qDebug()<<"socket ioio closed";
}

void IOIO_UDP_Object::write_pwm_values(int motor, int servo)
{
    pwm_motor = motor;
    pwm_servo = servo;
}

void IOIO_UDP_Object::read_data()
{
    while (udpSocket->hasPendingDatagrams())
    {
        QByteArray datagram;
        datagram.resize(udpSocket->pendingDatagramSize());
        QHostAddress sender;
        quint16 senderPort;

        udpSocket->readDatagram(datagram.data(), datagram.size(),&sender, &senderPort);

        short nb = (short)((datagram.at(0) & 0xff) << 8 | (datagram.at(1) & 0xff));
        ir1 = (float)nb / 10000;

        nb = (short)((datagram.at(2) & 0xff) << 8 | (datagram.at(3) & 0xff));
        ir2 = (float)nb / 10000;

        nb = (short)((datagram.at(4) & 0xff) << 8 | (datagram.at(5) & 0xff));
        ir3 = (float)nb / 10000;

        nb = (short)((datagram.at(6) & 0xff) << 8 | (datagram.at(7) & 0xff));
        ir4 = (float)nb / 10000;

        emit IR_val_ready(ir1, ir2, ir3, ir4);
        datagram.clear();

//        qDebug()<<"motor: " << pwm_motor << ", servo: " << pwm_servo;

        qint8 byte_mode = 0;
        datagram.append(byte_mode);
        datagram.append((qint8) (pwm_servo >> 8));
        datagram.append((qint8) pwm_servo);
        datagram.append((qint8) (pwm_motor >> 8));
        datagram.append((qint8) pwm_motor);
        udpSocket->writeDatagram(datagram,sender,senderPort);
        udpSocket->waitForBytesWritten();
    }
}
