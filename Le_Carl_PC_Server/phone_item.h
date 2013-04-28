#ifndef PHONE_ITEM_H
#define PHONE_ITEM_H

#include <QObject>
#include <QGraphicsItem>
#include <QPainter>
#include <QDebug>

class Phone_item : public QObject, public QGraphicsItem
{
  Q_OBJECT

public:
    Phone_item();
    void paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget);
    QRectF boundingRect() const;

public:
    QPixmap a_pixmap;
    QPixmap android_logo;

signals:
    void show_dialog();

public slots:
    void display_pix(QImage ima, int size);

protected:
    void mouseReleaseEvent(QGraphicsSceneMouseEvent *event);
};

#endif // PHONE_ITEM_H
