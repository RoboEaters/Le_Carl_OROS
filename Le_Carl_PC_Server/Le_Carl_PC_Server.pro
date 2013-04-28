#-------------------------------------------------
#
# Project created by QtCreator 2012-04-09T20:43:47
#
#-------------------------------------------------

QT       += core gui
QT       += network
QT       += opengl

TARGET = Le_Carl_PC_Server
TEMPLATE = app


SOURCES += main.cpp\
        servermainwindow.cpp \
    phone_item.cpp \
    android_dialog.cpp \
    camera_udp_object.cpp \
    android_tcp_object.cpp \
    android_tcp_server.cpp \
    sensors_udp_object.cpp \
    ioio_udp_object.cpp \
    nn_udp_object.cpp

HEADERS  += servermainwindow.h \
    phone_item.h \
    android_dialog.h \
    camera_udp_object.h \
    android_tcp_object.h \
    android_tcp_server.h \
    sensors_udp_object.h \
    ioio_udp_object.h \
    nn_udp_object.h

FORMS    += servermainwindow.ui \
    android_dialog.ui

RESOURCES += \
    CARL_resources.qrc
