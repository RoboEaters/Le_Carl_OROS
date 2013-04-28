#include "android_tcp_object.h"
#include "servermainwindow.h"
#include <QStringList>
#include <QString>
#include <QDebug>
#include <QByteArray>
#include <QThread>
#include <QTimer>

Android_TCP_Object::Android_TCP_Object(int ID, int nb)
{
    socketDescriptor = ID;
    idx_phone = nb;
}

void Android_TCP_Object::init_socket()
{
    qDebug() << socketDescriptor << " Starting TCP thread for Android idx:" << idx_phone;
    socket = new QTcpSocket();
    if(!socket->setSocketDescriptor(socketDescriptor)) return;

    ip_server = socket->localAddress();

    connect(socket,SIGNAL(readyRead()),this,SLOT(Read_data()));
    connect(socket,SIGNAL(disconnected()),this,SLOT(disconnected_slot()), Qt::DirectConnection);

    qDebug() << socketDescriptor << " Client Connected";
}

void Android_TCP_Object::Read_data()
{
    qDebug() << socketDescriptor << " reading...";

    QTextStream in(socket);
    QString s;
    s = in.readAll();
    qDebug() << socketDescriptor << " received..." << s;

    QStringList a_list = s.split("/");
    QString flag = a_list.at(0);

    if(flag == "PHONE")
    {
        QHostAddress addr = socket->peerAddress();
        name = a_list.at(1);
        IP = addr.toString();
        port_TCP = socket->peerPort();

        a_list.removeAt(0);
        a_list.removeAt(0);
        a_list.removeLast();
        list_sizes_cam = QStringList(a_list);

        emit new_phone(this);

        QString the_text = QString::number(idx_phone);
        the_text.append("\n");
        QByteArray by = the_text.toAscii();
        socket->write(by);              //send idx phone back
    }
}

void Android_TCP_Object::disconnect_phone()
{
    qDebug() << socketDescriptor << " Closing socket from PC";
    socket->close();        //will emit disconnected...calling disconnected_slot()
}

void Android_TCP_Object::disconnected_slot()
{
    emit phone_disconnected(idx_phone);

    socket->deleteLater();
    this->deleteLater();
    qDebug() << socketDescriptor << " Disconnected \n";
}

void Android_TCP_Object::send_message(QString msg)
{
    if(socket->isOpen())
    {
        qDebug() << socketDescriptor << " Sending " << msg;
        socket->write(msg.toAscii());              //send idx phone back
        socket->flush();
    }
    else qDebug() << socketDescriptor << " socket not open...can't write ";
}
