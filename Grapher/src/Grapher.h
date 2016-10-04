#ifndef GRAPHER_H
#define GRAPHER_H

#include "GrapherExpression.h"

typedef struct Grapher Grapher;

struct Grapher {
    GrapherExpression* expr;

    int width, height;
    int xOffset, yOffset;
    double xScaleInterval, yScaleInterval;
};

#define GPOINT_NONE 0
#define GPOINT_YES 1
#define GPOINT_GAP 2

/**
 * @brief genPoints
 * @param gb the Grapher struct
 * @return Array of GPOINTs. Order: from top to bottom, then from left to right
 */
char* genPoints(Grapher* gb);

#endif // GRAPHER_H
