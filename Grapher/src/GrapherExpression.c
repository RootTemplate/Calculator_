#include "GrapherExpression.h"
#include "GrapherExpressionOpcodes.h"
#include "GrapherDPoints.h"
#include <stdlib.h>
#include <math.h>
#include "MoreMath.h"
#include <stdio.h>

struct GrapherExpression {
    int* code;
    double* buffer;
    double* stack;

    int codeLength;
    int xIndex, yIndex;
    byte gaps[2];
};

typedef struct GapKitElem GapKitElem;
struct GapKitElem {
    double before, prev;
    byte dPoint;
};

GrapherExpression* createExpr(int* code, double* buffer, int codeLength, int stackLength, int xIndex, int yIndex)
{
    GrapherExpression* expr = (GrapherExpression*) malloc(sizeof(GrapherExpression));
    expr->code = code;
    expr->buffer = buffer;
    expr->stack = (double*) malloc(stackLength * sizeof(double));
    expr->codeLength = codeLength;
    expr->xIndex = xIndex;
    expr->yIndex = yIndex;
    expr->gaps[0] = expr->gaps[1] = 0;
    return expr;
}

void *createGapKit(GrapherExpression *expr)
{
    int gapKitElems = 0;
    int* code = expr->code;
    int i;
    for(i = 0; i < expr->codeLength; i++)
        switch(code[i]) {
        case EOP_DIV:
            gapKitElems += 2;
            break;

        default:
            break;
        }

    if(gapKitElems == 0)
        return NULL;

    GapKitElem* result = (GapKitElem*) malloc(gapKitElems * sizeof(GapKitElem));
    for(i = 0; i < gapKitElems; i++) {
        result[i].before = NaN;
        result[i].prev = NaN;
        result[i].dPoint = 0;
    }

    return result;
}

void setExprX(GrapherExpression *expr, double x)
{
    expr->buffer[expr->xIndex] = x;
}

double evalExpr(GrapherExpression *expr, double y, byte gapsType, void **gapKits)
{
    int* code = expr->code;
    double* buffer = expr->buffer;
    double* stack = expr->stack;
    int stackIndex = -1, kitsIndex = 0;
    GapKitElem** kits = (GapKitElem**) gapKits;

    buffer[expr->yIndex] = y;

    int i, j;
    for(i = 0; i < expr->codeLength; ++i)
    {
        int insn = code[i];
        if(insn < 0) {
            switch(insn) {
            case EOP_ADD:
                stack[stackIndex - 1] += stack[stackIndex];
                --stackIndex;
                break;
            case EOP_SUB:
                stack[stackIndex - 1] -= stack[stackIndex];
                --stackIndex;
                break;
            case EOP_MUL:
                stack[stackIndex - 1] *= stack[stackIndex];
                --stackIndex;
                break;
            case EOP_DIV:
                // a / b = c
                if(gapsType == 0) {
                    for(j = 0; j < 2; ++j) {
                        GapKitElem* a = &kits[j][kitsIndex];
                        GapKitElem* b = &kits[j][kitsIndex + 1];
                        b->dPoint = getDPoint(b->before, b->prev, stack[stackIndex]);
                        if(b->dPoint >= DPOINT_MIDDLE) {
                            expr->gaps[j] = 1;
                            a->dPoint = getDPoint(a->before, a->prev, stack[stackIndex - 1]);
                        }
                        a->before = a->prev;
                        a->prev = stack[stackIndex - 1];
                        b->before = b->prev;
                        b->prev = stack[stackIndex];
                    }
                    stack[stackIndex - 1] /= stack[stackIndex];
                } else if(gapsType == 1 || gapsType == 2) {
                    GapKitElem* a = &kits[0][kitsIndex];
                    GapKitElem* b = &kits[0][kitsIndex + 1];
                    double result;
                    if(b->dPoint >= DPOINT_MIDDLE) {
                        if(a->dPoint >= DPOINT_MIDDLE) {
                            double bNext = b->prev * (b->prev < 0 ? -1 : 1);
                            double bPrev = b->before * (b->before < 0 ? -1 : 1);
                            double resNext = a->prev / b->prev;
                            double resPrev = a->before / b->before;

                            result = resPrev + (resNext - resPrev) * (bNext / (bNext + bPrev));
                        } else {
                            short denSign, numSign;
                            if(gapsType == 1) {
                                denSign = b->before >= 0 ? 1 : -1;
                                numSign = a->before >= 0 ? 1 : -1;
                                if(a->before == 0 && b->before == 0)
                                    denSign = numSign = 0;
                            } else {
                                denSign = b->prev >= 0 ? 1 : -1;
                                numSign = a->prev >= 0 ? 1 : -1;
                                if(a->prev == 0 && b->prev == 0)
                                    denSign = numSign = 0;
                            }
                            if(stack[stackIndex] - 1 == stack[stackIndex]) {
                                // b is inf or -inf and not NaN
                                // b is very big, so the result must be very small
                                result = 0;
                            } else
                                result = numSign * denSign / (double) 0;
                        }
                    } else
                        result = stack[stackIndex - 1] / stack[stackIndex];

                    if(gapsType == 2) {
                        a->before = stack[stackIndex - 1];
                        b->before = stack[stackIndex];
                    }

                    stack[stackIndex - 1] = result;
                } else
                    stack[stackIndex - 1] /= stack[stackIndex];
                --stackIndex;
                kitsIndex += 2;
                break;
            case EOP_REM:
                stack[stackIndex - 1] = fmod(stack[stackIndex - 1], stack[stackIndex]);
                --stackIndex;
                break;
            case EOP_POW:
                stack[stackIndex - 1] = pow(stack[stackIndex - 1], stack[stackIndex]);
                --stackIndex;
                break;
            case EOP_ROOT:
                stack[stackIndex - 1] = root(stack[stackIndex - 1], round(stack[stackIndex]));
                --stackIndex;
                break;

            case EOP_NEG:
                stack[stackIndex] *= -1;
                break;
            case EOP_FACT:
                stack[stackIndex] = factorial(stack[stackIndex]);
                break;

            case EOP_ROUND:
                stack[stackIndex] = round(stack[stackIndex]);
                break;
            case EOP_FLOOR:
                stack[stackIndex] = floor(stack[stackIndex]);
                break;
            case EOP_CEIL:
                stack[stackIndex] = ceil(stack[stackIndex]);
                break;
            case EOP_TORAD:
                stack[stackIndex] = toRadians(stack[stackIndex]);
                break;
            case EOP_TODEG:
                stack[stackIndex] = toDegrees(stack[stackIndex]);
                break;
            case EOP_LOG10:
                stack[stackIndex] = log10(stack[stackIndex]);
                break;
            case EOP_LOG:
                stack[stackIndex] = log(stack[stackIndex]);
                break;
            case EOP_ABS:
                stack[stackIndex] = abs(stack[stackIndex]);
                break;
            case EOP_SQRT:
                stack[stackIndex] = sqrt(stack[stackIndex]);
                break;
            case EOP_CBRT:
                stack[stackIndex] = cbrt(stack[stackIndex]);
                break;

            case EOP_SIN:
                stack[stackIndex] = sin1(stack[stackIndex]);
                break;
            case EOP_COS:
                stack[stackIndex] = cos1(stack[stackIndex]);
                break;
            case EOP_TAN:
                stack[stackIndex] = tan1(stack[stackIndex]);
                break;
            case EOP_ASIN:
                stack[stackIndex] = asin(stack[stackIndex]);
                break;
            case EOP_ACOS:
                stack[stackIndex] = acos(stack[stackIndex]);
                break;
            case EOP_ATAN:
                stack[stackIndex] = atan(stack[stackIndex]);
                break;
            }
        } else {
            ++stackIndex;
            stack[stackIndex] = buffer[insn];
        }
    }

    return stack[0];
}

byte *getExprGapsStorage(GrapherExpression *expr)
{
    return expr->gaps;
}

void freeExpr(GrapherExpression *expr, int freeCodeAndBuffer)
{
    free(expr->stack);
    if(freeCodeAndBuffer) {
        free(expr->code);
        free(expr->buffer);
    }
    free(expr);
}

void freeGapKit(void *gapKit)
{
    free(gapKit);
}
