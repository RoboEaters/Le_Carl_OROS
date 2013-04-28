#ifndef ANDROID_TCP_OBJECT_H
#define ANDROID_TCP_OBJECT_H

#include <QObject>
#include <QTcpSocket>
#include <QDebug>
#include <QHostAddress>
#include <QStringList>

class Android_TCP_Object : public QObject
{
    Q_OBJECT

public:
    explicit Android_TCP_Object(int ID, int nb);

signals:
    void new_phone(Android_TCP_Object* thread);
    void phone_disconnected(int idx);
    void received_msg(QString msg);

public slots:
    void Read_data();
    void disconnected_slot();
    void disconnect_phone();
    void send_message(QString msg);
    void init_socket();

public:
    QHostAddress ip_server;

    QTcpSocket *socket;
    QString name, IP;
    quint16 port_TCP, port_sensor, port_camera, port_IOIO;
    int idx_phone;
    int socketDescriptor;
    QStringList list_sizes_cam;
    quint16 blockSize;
};

#endif // ANDROID_TCP_OBJECT_H
