#ifndef IOIO_UDP_OBJECT_H
#define IOIO_UDP_OBJECT_H

#include <QObject>
#include <QUdpSocket>

class IOIO_UDP_Object : public QObject
{
    Q_OBJECT
public:
    explicit IOIO_UDP_Object(QHostAddress ip, quint16 port);

signals:
    void IR_val_ready(float, float, float, float);

public slots:
    void read_data();
    void close_socket();
    void write_pwm_values(int motor, int servo);

private:
    QUdpSocket *udpSocket;
    float ir1, ir2, ir3, ir4;
    int pwm_motor, pwm_servo;
};
#endif // IOIO_UDP_OBJECT_H
