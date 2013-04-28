#include "android_dialog.h"
#include "ui_android_dialog.h"
#include "android_tcp_object.h"
#include "camera_udp_object.h"
#include "sensors_udp_object.h"
#include "ioio_udp_object.h"
#include "nn_udp_object.h"
#include "phone_item.h"
#include <QThread>
#include <QKeyEvent>
#include <QDateTime>

Android_Dialog::Android_Dialog(QWidget *parent, Android_TCP_Object *android_obj, Phone_item *phone_item, int port_seed) :
    QDialog(parent),
    ui(new Ui::Android_Dialog)
{
    ui->setupUi(this);
    android_tcp = android_obj;
    the_phone_item = phone_item;

    counter = 0;

    //    setModal(true);
//        ui->video_label->setScaledContents(true);

    connect(the_phone_item,SIGNAL(show_dialog()), this, SLOT(show()));

    connect(ui->sensors_start_btn,SIGNAL(clicked()),this,SLOT(start_sensors_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(start_sensors(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->sensors_stop_btn,SIGNAL(clicked()),this,SLOT(stop_sensors_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(stop_sensors(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->camera_start_btn,SIGNAL(clicked()),this,SLOT(start_camera_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(start_video(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->camera_stop_btn,SIGNAL(clicked()),this,SLOT(stop_camera_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(stop_video(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->img_compression_Slider,SIGNAL(valueChanged(int)),this,SLOT(set_comp_rate(int)),Qt::DirectConnection);
    connect(this,SIGNAL(send_img_rate(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->ioio_start_btn,SIGNAL(clicked()),this,SLOT(start_ioio_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(start_ioio(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->ioio_stop_btn,SIGNAL(clicked()),this,SLOT(stop_ioio_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(stop_ioio(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->start_exp_btn,SIGNAL(clicked()),this,SLOT(start_exp_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(start_exp(QString)),android_tcp,SLOT(send_message(QString)));

    connect(ui->stop_exp_btn,SIGNAL(clicked()),this,SLOT(stop_exp_phone()),Qt::DirectConnection);
    connect(this,SIGNAL(stop_exp(QString)),android_tcp,SLOT(send_message(QString)));

    connect(android_tcp,SIGNAL(received_msg(QString)),this,SLOT(display_msg(QString)));

    QString title;
    title.append("Android ");
    title.append(QString::number(android_tcp->idx_phone));
    setWindowTitle(title);

    QPixmap pix;
    pix.load(":/Images/Image_files/carl_logo.jpg");
    ui->video_label->setPixmap(pix);
    pix.alphaChannel();

    QPixmap alpha;
    QPainter *p;

    left_pix_pressed.load(":/Images/Image_files/left_arrow.png");
    left_pix_released = left_pix_pressed.copy();
    alpha = left_pix_released;
    p = new QPainter(&alpha);
    p->fillRect(alpha.rect(), QColor(50, 50, 50));
    p->end();
    left_pix_released.setAlphaChannel(alpha);
    ui->left_label->setPixmap(left_pix_released);

    right_pix_pressed.load(":/Images/Image_files/right_arrow.png");
    right_pix_released = right_pix_pressed.copy();
    alpha = right_pix_released;
    p = new QPainter(&alpha);
    p->fillRect(alpha.rect(), QColor(50, 50, 50));
    p->end();
    right_pix_released.setAlphaChannel(alpha);
    ui->right_label->setPixmap(right_pix_released);

    up_pix_pressed.load(":/Images/Image_files/up_arrow.png");
    up_pix_released = up_pix_pressed.copy();
    alpha = up_pix_released;
    p = new QPainter(&alpha);
    p->fillRect(alpha.rect(), QColor(50, 50, 50));
    p->end();
    up_pix_released.setAlphaChannel(alpha);
    ui->up_label->setPixmap(up_pix_released);

    down_pix_pressed.load(":/Images/Image_files/down_arrow.png");
    down_pix_released = down_pix_pressed.copy();
    alpha = down_pix_released;
    p = new QPainter(&alpha);
    p->fillRect(alpha.rect(), QColor(50, 50, 50));
    p->end();
    down_pix_released.setAlphaChannel(alpha);
    ui->down_label->setPixmap(down_pix_released);

    up_key_pressed = false;
    left_key_pressed = false;
    down_key_pressed = false;
    right_key_pressed = false;

    ui->phone_label->setText(android_tcp->name);
    ui->IP_label->setText(android_tcp->IP);
    ui->camera_stop_btn->setEnabled(false);
    ui->img_compression_Slider->setEnabled(false);
    ui->cam_sizes_box->insertItems(0,android_tcp->list_sizes_cam);
    ui->cam_sizes_box->setCurrentIndex(android_tcp->list_sizes_cam.size()-1);
    ui->cam_port_lineEdit->setText(QString::number(port_seed));
    ui->sensors_port_lineEdit->setText(QString::number(port_seed+1));
    ui->ioio_port_lineEdit->setText(QString::number(port_seed+2));
    ui->NN_port_lineEdit->setText(QString::number(port_seed+3));

    ui->motor_progressBar->setEnabled(false);
    ui->servo_progressBar->setEnabled(false);
    ui->sensors_stop_btn->setEnabled(false);
    ui->ioio_stop_btn->setEnabled(false);

    this->adjustSize();

    timer_ioio_pwm = new QTimer(this);
    connect(timer_ioio_pwm, SIGNAL(timeout()), this, SLOT(upd_ioio_pwm()));
}

Android_Dialog::~Android_Dialog()
{

    delete ui;
}

void Android_Dialog::delete_phone_dialog()
{
    emit stop_sensors("SENSORS_OFF\n");
    emit stop_video("CAMERA_OFF\n");
    emit close_upd_sensors();
    emit close_upd_cam();
//    the_phone_item->deleteLater();
//    this->deleteLater();
}

void Android_Dialog::start_sensors_phone()
{
    QString p = ui->sensors_port_lineEdit->text();
    if(!p.isEmpty())
    {
        ui->sensors_start_btn->setEnabled(false);
        ui->sensors_stop_btn->setEnabled(true);
        ui->sensors_port_lineEdit->setEnabled(false);

        qDebug()<<"port sensors: " << p;
        port_sensor = p.toUInt();
        Sensors_UDP_Object *sensors_obj = new Sensors_UDP_Object(android_tcp->ip_server,port_sensor);
        QThread *a_thread = new QThread;

        connect(a_thread, SIGNAL(finished()),a_thread, SLOT(deleteLater()));
        connect(sensors_obj, SIGNAL(values_ready(QString)), this, SLOT(display_sensors(QString)));
        connect(this,SIGNAL(close_upd_sensors()), sensors_obj,SLOT(close_socket()));

        sensors_obj->moveToThread(a_thread);
        a_thread->start();

        QString ss;
        ss.append("SENSORS_ON/");
        ss.append(p);
        ss.append("\n");

        emit start_sensors(ss);
    }
}

void Android_Dialog::stop_sensors_phone()
{
    emit close_upd_sensors();
    emit stop_sensors("SENSORS_OFF\n");

    ui->sensors_start_btn->setEnabled(true);
    ui->sensors_stop_btn->setEnabled(false);
    ui->sensors_port_lineEdit->setEnabled(true);
}

void Android_Dialog::start_camera_phone()
{
    QString p = ui->cam_port_lineEdit->text();
    if(!p.isEmpty())
    {
        ui->camera_start_btn->setEnabled(false);
        ui->camera_stop_btn->setEnabled(true);
        ui->cam_port_lineEdit->setEnabled(false);
        ui->cam_sizes_box->setEnabled(false);
        ui->img_compression_Slider->setEnabled(true);

        qDebug()<<"port cam: " << p;
        port_camera = p.toUInt();
        Camera_UDP_Object *cam_obj = new Camera_UDP_Object(android_tcp->ip_server,port_camera);
        QThread *a_thread = new QThread;

        connect(a_thread, SIGNAL(finished()),a_thread, SLOT(deleteLater()));
        connect(cam_obj, SIGNAL(frame_ready(QImage, int)), this, SLOT(display_frame(QImage, int)));
        connect(cam_obj, SIGNAL(frame_ready(QImage, int)),the_phone_item, SLOT(display_pix(QImage, int)));
        connect(this,SIGNAL(close_upd_cam()), cam_obj,SLOT(close_socket()));

        cam_obj->moveToThread(a_thread);
        a_thread->start();

        QString ss;
        ss.append("CAMERA_ON/");
        ss.append(p);
        ss.append("/");
        ss.append(QString::number(ui->cam_sizes_box->currentIndex()));
        ss.append("\n");

        emit start_video(ss);
    }
    else
        qDebug()<<"port empty";
}

void Android_Dialog::stop_camera_phone()
{
    emit close_upd_cam();
    emit stop_video("CAMERA_OFF\n");

    ui->camera_stop_btn->setEnabled(false);
    ui->camera_start_btn->setEnabled(true);
    ui->cam_port_lineEdit->setEnabled(true);
    ui->cam_sizes_box->setEnabled(true);
    ui->img_compression_Slider->setValue(50);
    ui->img_compression_Slider->setEnabled(false);
}

void Android_Dialog::start_ioio_phone()
{
    QString p = ui->ioio_port_lineEdit->text();
    if(!p.isEmpty())
    {
        ui->ioio_checkBox->setEnabled(false);
        ui->ioio_start_btn->setEnabled(false);
        ui->ioio_stop_btn->setEnabled(true);
        ui->ioio_port_lineEdit->setEnabled(false);

        min_motor = ui->motor_min_lineEdit->text().toInt();
        max_motor = ui->motor_max_lineEdit->text().toInt();
        min_servo = ui->servo_min_lineEdit->text().toInt();
        max_servo = ui->servo_max_lineEdit->text().toInt();
        default_servo = ui->default_servo_lineEdit->text().toInt();
        default_motor = ui->default_motor_lineEdit->text().toInt();
        step_servo =  ui->step_servo_lineEdit->text().toInt();
        step_motor = ui->step_motor_lineEdit->text().toInt();

        pwm_servo = default_servo;
        pwm_motor = default_motor;

        ui->motor_progressBar->setMinimum(min_motor);
        ui->servo_progressBar->setMinimum(min_servo);
        ui->motor_progressBar->setMaximum(max_motor);
        ui->servo_progressBar->setMaximum(max_servo);

        ui->motor_progressBar->setValue(default_motor);
        ui->servo_progressBar->setValue(default_servo);

        ui->servo_min_lineEdit->setEnabled(false);
        ui->motor_min_lineEdit->setEnabled(false);
        ui->servo_max_lineEdit->setEnabled(false);
        ui->motor_max_lineEdit->setEnabled(false);
        ui->default_servo_lineEdit->setEnabled(false);
        ui->default_motor_lineEdit->setEnabled(false);
        ui->motor_progressBar->setEnabled(true);
        ui->servo_progressBar->setEnabled(true);
        ui->step_servo_lineEdit->setEnabled(false);
        ui->step_motor_lineEdit->setEnabled(false);

        qDebug()<<"port ioio: " << p;
        port_IOIO = p.toUInt();

        IOIO_UDP_Object * the_ioio = new IOIO_UDP_Object(android_tcp->ip_server,port_IOIO);
        QThread *a_thread = new QThread;

        connect(a_thread, SIGNAL(finished()),a_thread, SLOT(deleteLater()));
        connect(the_ioio, SIGNAL(IR_val_ready(float,float,float,float)), this, SLOT(display_IR(float,float,float,float)));
        connect(this,SIGNAL(close_upd_ioio()), the_ioio,SLOT(close_socket()));
        connect(this,SIGNAL(send_ioio_pwm(int,int)),the_ioio,SLOT(write_pwm_values(int,int)));

        the_ioio->moveToThread(a_thread);
        a_thread->start();

        bool b = ui->ioio_checkBox->isChecked();

        QString ss;
        ss.append("IOIO_ON/");
        ss.append(p);
        ss.append("/");
        ss.append(QString::number(b));
        ss.append("\n");

        emit start_ioio(ss);

        timer_ioio_pwm->start(100);
    }
}
void Android_Dialog::stop_ioio_phone()
{
    timer_ioio_pwm->stop();
    emit close_upd_ioio();
    emit stop_ioio("IOIO_OFF\n");

    ui->motor_progressBar->setValue(default_motor);
    ui->servo_progressBar->setValue(default_servo);

    ui->ioio_checkBox->setEnabled(true);
    ui->ioio_start_btn->setEnabled(true);
    ui->ioio_stop_btn->setEnabled(false);
    ui->ioio_port_lineEdit->setEnabled(true);
    ui->servo_min_lineEdit->setEnabled(true);
    ui->motor_min_lineEdit->setEnabled(true);
    ui->servo_max_lineEdit->setEnabled(true);
    ui->motor_max_lineEdit->setEnabled(true);
    ui->default_servo_lineEdit->setEnabled(true);
    ui->default_motor_lineEdit->setEnabled(true);
    ui->motor_progressBar->setEnabled(false);
    ui->servo_progressBar->setEnabled(false);
    ui->step_servo_lineEdit->setEnabled(true);
    ui->step_motor_lineEdit->setEnabled(true);
}

void Android_Dialog::start_exp_phone()
{
    timer_data = new QTimer(this);
    connect(timer_data, SIGNAL(timeout()), this, SLOT(save_data()));
    timer_data->start(500);  //500 ms

    ui->start_exp_btn->setEnabled(false);
    ui->stop_exp_btn->setEnabled(true);
//    ui->ioio_start_btn->setEnabled(false);
//    ui->ioio_stop_btn->setEnabled(false);
//    ui->ioio_port_lineEdit->setEnabled(false);
//    ui->servo_min_lineEdit->setEnabled(false);
//    ui->motor_min_lineEdit->setEnabled(false);
//    ui->servo_max_lineEdit->setEnabled(false);
//    ui->motor_max_lineEdit->setEnabled(false);
//    ui->default_servo_lineEdit->setEnabled(false);
//    ui->default_motor_lineEdit->setEnabled(false);
//    ui->motor_progressBar->setEnabled(false);
//    ui->servo_progressBar->setEnabled(false);
//    ui->step_servo_lineEdit->setEnabled(false);
//    ui->step_motor_lineEdit->setEnabled(false);
////    ui->sensors_start_btn->setEnabled(false);
////    ui->sensors_stop_btn->setEnabled(false);
////    ui->sensors_port_lineEdit->setEnabled(false);
//    ui->NN_port_lineEdit->setEnabled(false);

//    bool b = ui->ioio_checkBox->isChecked();
//    QString p = ui->ioio_port_lineEdit->text();
//    QString p2 = ui->NN_port_lineEdit->text();

//    port_IOIO = p.toUInt();
//    port_NN = p2.toUInt();

//    IOIO_UDP_Object * the_ioio = new IOIO_UDP_Object(android_tcp->ip_server,port_IOIO);
//    QThread *a_thread = new QThread;

//    NN_udp_object * the_NN = new NN_udp_object(android_tcp->ip_server,port_NN);
//    QThread *a_thread2 = new QThread;

//    connect(a_thread, SIGNAL(finished()),a_thread, SLOT(deleteLater()));
//    connect(the_ioio, SIGNAL(IR_val_ready(float,float,float,float)), this, SLOT(display_IR(float,float,float,float)));
//    connect(this,SIGNAL(close_upd_ioio()), the_ioio,SLOT(close_socket()));

//    connect(a_thread2, SIGNAL(finished()),a_thread2, SLOT(deleteLater()));
//    connect(the_NN, SIGNAL(values_ready(QString)), this, SLOT(display_msg(QString)));
//    connect(this,SIGNAL(close_udp_NN()), the_NN,SLOT(close_socket()));

//    the_ioio->moveToThread(a_thread);
//    the_NN->moveToThread(a_thread2);
//    a_thread->start();
//    a_thread2->start();

//    QString ss;
//    ss.append("EXP_ON/");
//    ss.append(p);
//    ss.append("/");
//    ss.append(p2);
//    ss.append("/");
//    ss.append(QString::number(b));
//    ss.append("\n");

//    emit start_exp(ss);
}

void Android_Dialog::stop_exp_phone()
{
    timer_data->stop();

    ui->start_exp_btn->setEnabled(true);
    ui->stop_exp_btn->setEnabled(false);
//    ui->ioio_start_btn->setEnabled(true);
//    ui->ioio_stop_btn->setEnabled(false);
//    ui->ioio_port_lineEdit->setEnabled(true);
//    ui->servo_min_lineEdit->setEnabled(true);
//    ui->motor_min_lineEdit->setEnabled(true);
//    ui->servo_max_lineEdit->setEnabled(true);
//    ui->motor_max_lineEdit->setEnabled(true);
//    ui->default_servo_lineEdit->setEnabled(true);
//    ui->default_motor_lineEdit->setEnabled(true);
//    ui->motor_progressBar->setEnabled(false);
//    ui->servo_progressBar->setEnabled(false);
//    ui->step_servo_lineEdit->setEnabled(true);
//    ui->step_motor_lineEdit->setEnabled(true);
//    ui->sensors_start_btn->setEnabled(true);
//    ui->sensors_stop_btn->setEnabled(false);
//    ui->sensors_port_lineEdit->setEnabled(true);
//    ui->NN_port_lineEdit->setEnabled(true);

//    emit close_udp_NN();
//    emit close_upd_ioio();
//    emit stop_exp("EXP_OFF\n");
}


void Android_Dialog::display_IR(float ir1, float ir2 ,float ir3, float ir4)
{
    ui->IR_front_LCD->display(ir1);
    ui->IR_left_LCD->display(ir2);
    ui->IR_right_LCD->display(ir3);
    ui->IR_back_LCD->display(ir4);
}

void Android_Dialog::set_comp_rate(int rate)
{
    img_compression_rate = rate;
    QString ss;
    QString s_rate;
    ss.append("IMG_RATE/");
    s_rate.append(QString::number(img_compression_rate));
    ss.append(s_rate);
    ss.append("\n");

    ui->compr_rate_label->setText(s_rate);

    emit send_img_rate(ss);
}

void Android_Dialog::display_frame(QImage ima, int size)
{
    ui->video_label->setPixmap(QPixmap::fromImage(ima));
    ui->size_image_label->setText(QString::number(size));
    this->adjustSize();
}

void Android_Dialog::display_sensors(QString values)
{
    sensors_values = values;

    QStringList a_list = sensors_values.split("/");

    compass_x = a_list.at(2).toFloat();
    compass_y = a_list.at(4).toFloat();
    compass_z = a_list.at(6).toFloat();

    accel_x = a_list.at(8).toFloat();
    accel_y = a_list.at(10).toFloat();
    accel_z = a_list.at(12).toFloat();

    ui->compass_LCD_x->display(compass_x);
    ui->compass_LCD_y->display(compass_y);
    ui->compass_LCD_z->display(compass_z);

    ui->accelerometer_LCD_x->display(accel_x);
    ui->accelerometer_LCD_y->display(accel_y);
    ui->accelerometer_LCD_z->display(accel_z);
}

void Android_Dialog::keyPressEvent(QKeyEvent *event)
{
    switch(event->key())
    {
    case Qt::Key_W: up_key_pressed = true;
        break;

    case Qt::Key_A:left_key_pressed = true;
        break;

    case Qt::Key_S: down_key_pressed = true;
        break;

    case Qt::Key_D: right_key_pressed = true;
        break;
    }
}
void Android_Dialog::keyReleaseEvent(QKeyEvent *event)
{
    switch(event->key())
    {
    case Qt::Key_W: up_key_pressed = false;
        break;

    case Qt::Key_A:left_key_pressed = false;
        break;

    case Qt::Key_S: down_key_pressed = false;
        break;

    case Qt::Key_D: right_key_pressed = false;
        break;
    }
}


void Android_Dialog::upd_ioio_pwm()
{
    if (left_key_pressed)
    {
        ui->left_label->setPixmap(left_pix_pressed);
        ui->right_label->setPixmap(right_pix_released);

        if(pwm_servo > min_servo) pwm_servo -= step_servo;
    }
    else if(right_key_pressed)
    {
        ui->left_label->setPixmap(left_pix_released);
        ui->right_label->setPixmap(right_pix_pressed);
        if(pwm_servo < max_servo) pwm_servo += step_servo;
    }
    else
    {
        ui->left_label->setPixmap(left_pix_released);
        ui->right_label->setPixmap(right_pix_released);
        if(pwm_servo < default_servo) pwm_servo+= step_servo;
        else if(pwm_servo > default_servo) pwm_servo-= step_servo;
    }

    if (up_key_pressed)
    {
        ui->up_label->setPixmap(up_pix_pressed);
        ui->down_label->setPixmap(down_pix_released);
        if(pwm_motor < max_motor) pwm_motor += step_motor;
    }
    else if (down_key_pressed)
    {
        ui->up_label->setPixmap(up_pix_released);
        ui->down_label->setPixmap(down_pix_pressed);
        if(pwm_motor > min_motor) pwm_motor -= step_motor;
    }
    else
    {
        ui->up_label->setPixmap(up_pix_released);
        ui->down_label->setPixmap(down_pix_released);
        if(pwm_motor < default_motor) pwm_motor += step_motor;
        else if(pwm_motor > default_motor) pwm_motor-= step_motor;
    }

    ui->motor_progressBar->setValue(pwm_motor);
    ui->servo_progressBar->setValue(pwm_servo);

    emit send_ioio_pwm(pwm_motor, pwm_servo);
}

void Android_Dialog::display_msg(QString msg)
{
    ui->nn_label->setText(msg);
}

void Android_Dialog::save_data()
{
    QDateTime date = QDateTime::currentDateTime();
    QString name_file;
    name_file.append("DATA/video_frames_11_4_2012/frame_");
    name_file.append(date.toString("MM_dd_hh_mm_ss"));
    name_file.append("_" + QString::number(counter)+".jpg");

    video_frame.save(name_file,"jpg",-1);

    name_file.clear();
    name_file.append("DATA/sensors_gps/");
    name_file.append(date.toString("MM_dd")+ "_sensors.txt");

    QFile file_data(name_file);
    file_data.open(QIODevice::Append | QIODevice::Text);
    QTextStream out(&file_data);
    out << date.toString("MM_dd_hh_mm_ss") + "\n";
    out << sensors_values;
    out.flush();
    file_data.close();

    counter++;
}



