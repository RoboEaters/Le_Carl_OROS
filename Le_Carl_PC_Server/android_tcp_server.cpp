#include "android_tcp_server.h"
#include "servermainwindow.h"
#include "android_tcp_object.h"
#include <QThread>

Android_TCP_Server::Android_TCP_Server(QObject *parent) :
    QTcpServer(parent)
{
    the_gui = (ServerMainWindow*)parent;
}

void Android_TCP_Server::StartServer(quint16 port, QHostAddress ip)
{
    port_tcp = port;
    IP_server = ip;
    nb_phone_connected = 0;

    if(!this->listen(IP_server,port_tcp))
    {
        qDebug() << "Could not start server";
    }
    else
    {
        qDebug() << "Server listening...";
    }
}

void Android_TCP_Server::incomingConnection(int socketDescriptor)
{
    qDebug() << socketDescriptor << " Client connecting...";

    nb_phone_connected++;

    Android_TCP_Object *a_phone = new Android_TCP_Object(socketDescriptor,nb_phone_connected);
    QThread *thread = new QThread;

    connect(thread,SIGNAL(started()),a_phone,SLOT(init_socket()));
    connect(thread, SIGNAL(finished()),thread, SLOT(deleteLater()));
    connect(a_phone, SIGNAL(new_phone(Android_TCP_Object*)),the_gui, SLOT(add_phone(Android_TCP_Object*)));
    connect(a_phone,SIGNAL(phone_disconnected(int)),the_gui,SLOT(remove_phone(int)));
    connect(the_gui, SIGNAL(stop_phone_theads()), a_phone, SLOT(disconnect_phone()));

    a_phone->moveToThread(thread);
    thread->start();
}
