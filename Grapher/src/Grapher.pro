#-------------------------------------------------
#
# Project created by QtCreator 2016-07-02T16:07:37
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = Grapher
TEMPLATE = app


SOURCES += main.cpp\
        mainwindow.cpp\
    GrapherExpression.c \
    MoreMath.c \
    Grapher.c \
    GrapherDPoints.c

HEADERS  += mainwindow.h\
    GrapherExpression.h \
    GrapherExpressionOpcodes.h \
    MoreMath.h \
    Grapher.h \
    GrapherDPoints.h \
    GrapherByte.h

#win32: LIBS += -LC:/Qt/Projects/GrapherEngine/build/Debug/debug/ -lGrapherEngine

#INCLUDEPATH += $$PWD/../GrapherEngine
#DEPENDPATH += $$PWD/../GrapherEngine
