#include <QtGui/QApplication>
#include "servermainwindow.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    ServerMainWindow w;
    w.show();
    
    return a.exec();
}
