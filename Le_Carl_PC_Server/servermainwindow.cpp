#include <QGLWidget>
#include <QNetworkInterface>
#include <QDebug>

#include "servermainwindow.h"
#include "ui_servermainwindow.h"
#include "android_dialog.h"
#include "phone_item.h"
#include "android_tcp_server.h"
#include "android_tcp_object.h"

ServerMainWindow::ServerMainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::ServerMainWindow)
{
    ui->setupUi(this);
    ui->stop_server_btn->setEnabled(false);

    scene = new QGraphicsScene(this);
    ui->the_graphicsView->setViewport(new QGLWidget(QGLFormat(QGL::DoubleBuffer)));
    ui->the_graphicsView->setScene(scene);

    init_IP_port();

    tcp_server = new Android_TCP_Server(this);

    connect(ui->start_server_btn,SIGNAL(clicked()),this,SLOT(start_server_thread()));
    connect(ui->stop_server_btn,SIGNAL(clicked()),this,SLOT(stop_server_thread()));    
}

void ServerMainWindow::init_IP_port()
{
    QList<QNetworkInterface> list= QNetworkInterface::allInterfaces();
    for(int i=0; i<list.size();i++)
    {
        QNetworkInterface inter = list.at(i);
        if (inter.flags().testFlag(QNetworkInterface::IsUp) && !inter.flags().testFlag(QNetworkInterface::IsLoopBack))
        {
            QList<QNetworkAddressEntry> list2= inter.addressEntries();
            for(int j=0; j<list2.size();j++)
            {
                QNetworkAddressEntry entry = list2.at(j);

                if ( inter.hardwareAddress() != "00:00:00:00:00:00" && entry.ip().toString().contains("."))
                {
                    IP_server = entry.ip();
                    qDebug() << inter.name() + " "+ entry.ip().toString() +" " + inter.hardwareAddress();
                    ui->IP_edit->setText(IP_server.toString());
                }
            }
        }
    }

    ui->port_edit->setText("9000");
}

ServerMainWindow::~ServerMainWindow()
{
    tcp_server->close();
    emit stop_phone_theads();
    delete ui;
}

void ServerMainWindow::start_server_thread()
{
    QString p = ui->port_edit->text();
    if(!p.isEmpty())
    {
        ui->port_edit->setEnabled(false);
        port_TCP = p.toUInt();

        tcp_server->StartServer(port_TCP, IP_server);
        ui->stop_server_btn->setEnabled(true);
        ui->start_server_btn->setEnabled(false);
    }
    else
        qDebug()<<"port empty";

}

void ServerMainWindow::stop_server_thread()
{
    ui->port_edit->setEnabled(true);
    ui->stop_server_btn->setEnabled(false);
    ui->start_server_btn->setEnabled(true);

    tcp_server->close();
    scene->clear();
    scene->update();

    emit stop_phone_theads();
}

void ServerMainWindow::add_phone(Android_TCP_Object* thread)
{
    int seed = port_TCP + (thread->idx_phone * 3);

    Phone_item *new_item = new Phone_item();
    Android_Dialog * new_dialog = new Android_Dialog(this, thread, new_item, seed);
    list_phone_dialogs.append(new_dialog);
    scene->addItem(new_item);
    scene->update();

    qDebug() << " scene has : "<< scene->items().size() << " items";
}

void ServerMainWindow::remove_phone(int idx)
{
    qDebug()<<"remove phone: " << idx;

    for(int nb=0; nb<list_phone_dialogs.size(); nb++)
    {
        Android_Dialog *a_dialog = list_phone_dialogs.at(nb);

        if(a_dialog->android_tcp->idx_phone == idx)
        {
            a_dialog->delete_phone_dialog();
            list_phone_dialogs.remove(nb);
            scene->removeItem(a_dialog->the_phone_item);
            a_dialog->close();
//            a_dialog->the_phone_item->deleteLater();
//            a_dialog->deleteLater();
        }
    }
    scene->update();
    qDebug() << "scene has : "<< scene->items().size() << " items";
}
