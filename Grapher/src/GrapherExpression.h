#ifndef GRAPHEREXPRESSION_H
#define GRAPHEREXPRESSION_H

#include "GrapherByte.h"

typedef struct GrapherExpression GrapherExpression;

// WARNING. GrapherExpression does NOT contain optionsAMU var. This means that sin, cos and tan
//          will receive arguments in RADIANS

GrapherExpression* createExpr(int* code, double* buffer, int codeLength, int stackLength,
                              int xIndex, int yIndex);
void *createGapKit(GrapherExpression* expr);
void setExprX(GrapherExpression* expr, double x);
double evalExpr(GrapherExpression* expr, double y, byte gapsType, void **gapKits);
byte* getExprGapsStorage(GrapherExpression* expr);
void freeExpr(GrapherExpression* expr, int freeCodeAndBuffer);
void freeGapKit(void* gapKit);

#endif // GRAPHEREXPRESSION_H
