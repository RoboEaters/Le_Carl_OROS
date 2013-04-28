#ifndef SENSORS_UDP_OBJECT_H
#define SENSORS_UDP_OBJECT_H

#include <QObject>
#include <QUdpSocket>

class Sensors_UDP_Object : public QObject
{
    Q_OBJECT
public:
    explicit Sensors_UDP_Object(QHostAddress ip, quint16 port);

signals:    
//    void values_ready(float, float, float, float, float, float, float, float, double, double, double);
//    void values_ready(float, float, float, float, float, float);
    void values_ready(QString);

public slots:
    void read_data();
    void close_socket();

private:
    QUdpSocket *udpSocket;
    float x_O,y_O,z_O,x_A,y_A,z_A;
    float speed, accuracy;
    double altitude, longitude, latitude;
};

#endif // SENSORS_UDP_OBJECT_H
