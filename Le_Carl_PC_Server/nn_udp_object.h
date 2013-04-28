#ifndef NN_UDP_OBJECT_H
#define NN_UDP_OBJECT_H

#include <QObject>
#include <QUdpSocket>
#include <QFile>

class NN_udp_object : public QObject
{
    Q_OBJECT
public:
    explicit NN_udp_object(QHostAddress ip, quint16 port);

signals:
        void values_ready(QString);

public slots:
    void read_data();
    void close_socket();

private:
    void save_data(QString data, QString path);

    QUdpSocket *udpSocket;
    QString msg;
    QString path;
};

#endif // NN_UDP_OBJECT_H
