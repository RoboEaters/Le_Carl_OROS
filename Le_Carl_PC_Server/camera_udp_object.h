#ifndef CAMERA_UDP_OBJECT_H
#define CAMERA_UDP_OBJECT_H

#include <QObject>
#include <QUdpSocket>
#include <QPixmap>
#include <QImage>

class Camera_UDP_Object : public QObject
{
    Q_OBJECT

public:
    explicit Camera_UDP_Object(QHostAddress ip, quint16 port);

signals:
    void frame_ready(QImage pix, int size_data);

public slots:
    void read_image();
    void close_socket();

private:
    QUdpSocket *udpSocket;

    int current_frame;
    int slicesStored;
    int nb_total_frames;
    QByteArray imageData;
    int size_packets;
    QVector<QByteArray> slices;

    QImage the_frame;
};

#endif // CAMERA_UDP_OBJECT_H
