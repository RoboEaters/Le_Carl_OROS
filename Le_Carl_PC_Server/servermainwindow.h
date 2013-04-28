#ifndef SERVERMAINWINDOW_H
#define SERVERMAINWINDOW_H

#include <QMainWindow>
#include <QGraphicsScene>
#include <QVector>
#include <QHostAddress>
#include <QPixmap>
#include <QMutex>

namespace Ui {
class ServerMainWindow;
}

class Android_TCP_Server;
class Android_TCP_Object;
class Android_Dialog;

class ServerMainWindow : public QMainWindow
{
    Q_OBJECT
    
public:
    explicit ServerMainWindow(QWidget *parent = 0);
    ~ServerMainWindow();
    void init_IP_port();
    
private:
    Ui::ServerMainWindow *ui;
    QGraphicsScene *scene;
    quint16 port_TCP;
    QMutex mutex;

public:
    Android_TCP_Server *tcp_server;
    QVector<Android_Dialog *> list_phone_dialogs;
    QHostAddress IP_server;

signals:
    void stop_phone_theads();

public slots:
    void start_server_thread();
    void stop_server_thread();
    void add_phone(Android_TCP_Object *thread);
    void remove_phone(int idx);
};

#endif // SERVERMAINWINDOW_H
