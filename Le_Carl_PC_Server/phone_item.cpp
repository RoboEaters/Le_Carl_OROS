#include "phone_item.h"
#include <QGraphicsSceneMouseEvent>

Phone_item::Phone_item()
{
    setFlag(ItemIsMovable);
    android_logo.load(":/Images/Image_files/android_logo2.jpg");
}

QRectF Phone_item::boundingRect() const
{
    return QRectF(0,0,100,100);
}

void Phone_item::paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget)
{
    QRectF rec = boundingRect();

    if(a_pixmap.isNull()==true) a_pixmap = android_logo.copy();

    painter->drawPixmap(rec,a_pixmap, a_pixmap.rect());
}

void Phone_item::mouseReleaseEvent(QGraphicsSceneMouseEvent *event)
{
    QGraphicsItem::mouseReleaseEvent(event);
    update();

    if(event->button() == Qt::RightButton)
        emit show_dialog();
}

void Phone_item::display_pix(QImage ima, int size)
{
    a_pixmap = QPixmap::fromImage(ima);
    update();
}
