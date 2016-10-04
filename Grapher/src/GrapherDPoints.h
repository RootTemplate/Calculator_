#ifndef GRAPHERDPOINTS_H
#define GRAPHERDPOINTS_H

#include "GrapherByte.h"

#define DPOINT_NO 0
#define DPOINT_WEAK 1
#define DPOINT_MIDDLE 2
#define DPOINT_STRONG 4

byte getDPoint(double before, double prev, double next);
double getComputingError();

#endif // GRAPHERDPOINTS_H
