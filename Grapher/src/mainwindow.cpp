#include "mainwindow.h"
#include <QPainter>
#include <QDebug>

extern "C" {
    #include "GrapherExpression.h"
    #include "GrapherExpressionOpcodes.h"
    #include "Grapher.h"
}

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
{
    this->resize(250, 250);
    blackPen = QPen(QColor(Qt::black));
    bluePen = QPen(QColor(Qt::blue));
    redPen = QPen(QColor(Qt::red));
}

MainWindow::~MainWindow()
{

}

void MainWindow::paintEvent(QPaintEvent *e)
{
    QPainter p(this);
    Grapher gb;

    // y = -x/x(x-0.2)
    /*
    int code[] = {0, EOP_NEG, 0, 0, 2, EOP_SUB, EOP_MUL, EOP_DIV, 1, EOP_SUB, 1, 3, EOP_SUB, EOP_MUL};
    int length = 14;
    //*/

    // y = 2(x+1)(x-1)/(x-1)
    /*
    int code[] = {0, 4, EOP_ADD, 0, 4, EOP_SUB, EOP_MUL, 5, EOP_MUL, 0, 4, EOP_SUB, EOP_DIV, 1, EOP_SUB};
    int length = 15;
    //*/

    // y = x^-2
    /*
    int code[] = {4, 0, 0, EOP_MUL, EOP_DIV, 1, 5, EOP_ADD, EOP_SUB};
    int length = 9;
    //*/

    // y = 1/(1/x)
    //*
    int code[] = {0, 6, 0, EOP_DIV, EOP_DIV, 1, EOP_SUB};
    int length = 7;
    //*/

    double buffer[] = {0, 0, 0.2, -2.3, 1, 2, 0};
    gb.expr = createExpr(code, buffer, length, 20, 0, 1);

    gb.width = width();
    gb.height = height();
    gb.xOffset = 0;
    gb.yOffset = 0;
    gb.xScaleInterval = 10;
    gb.yScaleInterval = 10;

    char* points = genPoints(&gb);
    int i;
    int width = gb.width, height = gb.height;
    //printf("W%d H%d\n", width / 2, height / 2);

    p.setPen(blackPen);
    p.drawLine(width / 2 - 1, 0, width / 2 - 1, height);
    p.drawLine(0, height / 2, width, height / 2);
    //p.drawLine(0, height / 2 - 50, width, height / 2 - 50);

    p.setPen(bluePen);
    for(i = 0; i < width * height; i++) {
        if(points[i] > 0) {
            int y = i % height;
            int x = (i - y) / height;
            p.drawPoint(x, y);
            if(points[i] == 2) {
                p.setPen(redPen);
                p.drawEllipse(x - 5, y - 5, 10, 10);
                p.setPen(bluePen);
            }
        }
    }

    p.setPen(blackPen);
    p.drawLine(gb.width, 0, gb.width, gb.height);
    p.drawLine(0, gb.height, gb.width, gb.height);
    free(points);

    freeExpr(gb.expr, 0);
    p.end();
}
