#ifndef ANDROID_DIALOG_H
#define ANDROID_DIALOG_H

#include <QDialog>
#include <QString>
#include <QHostAddress>
#include <QVector>
#include <QTimer>

namespace Ui {
class Android_Dialog;
}

class Android_TCP_Object;
class Camera_UDP_Object;
class Phone_item;

class Android_Dialog : public QDialog
{
    Q_OBJECT
    
public:
    explicit Android_Dialog(QWidget *parent = 0, Android_TCP_Object *android_obj=0, Phone_item *phone_item=0, int port_seed=0);
    ~Android_Dialog();
    void delete_phone_dialog();
    void keyPressEvent(QKeyEvent *event);
    void keyReleaseEvent(QKeyEvent * event);
    
private:
    Ui::Android_Dialog *ui;

public:
    Android_TCP_Object* android_tcp;
    Phone_item *the_phone_item;
    QString name, IP;
    quint16 port_TCP, port_sensor, port_camera, port_IOIO, port_NN;
    int idx_phone;
    QVector<QString*> cam_sizes;
    int idx_size_selected;
    float acceleration, compass;
    int img_compression_rate;
    int pwm_servo, pwm_motor;
    int min_servo, min_motor, max_servo, max_motor, default_servo, default_motor, step_servo, step_motor;
    QTimer *timer_ioio_pwm;
    bool up_key_pressed, left_key_pressed, down_key_pressed, right_key_pressed;
    QPixmap up_pix_pressed, down_pix_pressed, right_pix_pressed, left_pix_pressed;
    QPixmap up_pix_released, down_pix_released, right_pix_released, left_pix_released;

    //data to save
    QTimer *timer_data;
    int counter;
    QPixmap video_frame;
    float accel_x, accel_y, accel_z, compass_x, compass_y, compass_z;
    float GPS_long, GPS_lat, GPS_accu;
    QVector<QString*> wifi_APs;
    QString sensors_values;


signals:
    void start_exp(QString);
    void stop_exp(QString);
    void start_sensors(QString);
    void stop_sensors(QString);
    void start_video(QString);
    void stop_video(QString);
    void start_ioio(QString);
    void stop_ioio(QString);
    void close_upd_cam();
    void close_upd_sensors();
    void close_upd_ioio();
    void close_udp_NN();
    void send_img_rate(QString);
    void send_ioio_pwm(int, int);

public slots:
    void start_exp_phone();
    void stop_exp_phone();
    void start_sensors_phone();
    void stop_sensors_phone();
    void start_camera_phone();
    void stop_camera_phone();
    void start_ioio_phone();
    void stop_ioio_phone();
    void set_comp_rate(int rate);
    void display_frame(QImage ima, int size);
    void display_sensors(QString values);
    void display_IR(float ir1, float ir2, float ir3, float ir4);
    void upd_ioio_pwm();
    void display_msg(QString msg);
    void save_data();
};

#endif // ANDROID_DIALOG_H
