#ifndef ANDROID_TCP_SERVER_H
#define ANDROID_TCP_SERVER_H

#include <QTcpServer>
#include <QDebug>

class ServerMainWindow;
class Android_TCP_Object;

class Android_TCP_Server : public QTcpServer
{
    Q_OBJECT

public:
    Android_TCP_Server(QObject *parent = 0);
    void StartServer(quint16 port, QHostAddress ip);

signals:

public slots:

protected:
    void incomingConnection(int socketDescriptor);

public:
    ServerMainWindow *the_gui;
    Android_TCP_Object *android_object;
    quint16 port_tcp;
    QHostAddress IP_server;
    int nb_phone_connected;
};

#endif // ANDROID_TCP_SERVER_H
