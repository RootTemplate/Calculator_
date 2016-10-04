#include "Grapher.h"
#include <stdlib.h>
#include <stdio.h>
#include "GrapherDPoints.h"
#include "MoreMath.h"

byte processGap(byte gapExpr, byte topLeft, byte topRight, byte botLeft, byte botRight);

char *genPoints(Grapher *gb)
{
    int i, j;
    byte gapType;
    int width = gb->width;
    int height = gb->height;
    byte gapsSupported = 1; // TODO: NOT USED
    char* result = (char*) malloc(sizeof(char) * width * height);
    for(i = 0; i < width * height; ++i)
        result[i] = GPOINT_NONE;

    double** diffs = (double**) malloc(sizeof(double*) * 2);
    for(i = 0; i < 3; ++i)
        diffs[i] = (double*) malloc(sizeof(double) * (height + 2));

    byte** points = (byte**) malloc(sizeof(byte*) * 2);
    for(i = 0; i < 3; ++i)
        points[i] = (byte*) malloc(sizeof(byte) * height);

    byte** crossingX = (byte**) malloc(sizeof(byte*) * 3);
    for(i = 0; i < 3; ++i)
        crossingX[i] = (byte*) malloc(sizeof(byte) * (height + 1));

    byte** crossingY = (byte**) malloc(sizeof(byte*) * 2);
    for(i = 0; i < 2; ++i)
        crossingY[i] = (byte*) malloc(sizeof(byte) * (height + 1));

    // gapsExpr[n][m] is a mask: 1 - gap on X; 2 - gap on Y
    byte** gapsExpr = (byte**) malloc(sizeof(byte*) * 2);
    for(i = 0; i < 2; ++i)
        gapsExpr[i] = (byte*) malloc(sizeof(byte) * height);


    void* gapKits[2];
    void* singleGapKit[1];
    void* gapKitY = createGapKit(gb->expr);
    void** gapKitsX = malloc(sizeof(void*) * (height + 1));
    for(i = 0; i < height + 1; ++i)
        gapKitsX[i] = createGapKit(gb->expr);
    gapKits[1] = gapKitY;
    byte* exprGapsStr = getExprGapsStorage(gb->expr);

    for(i = -1; i < width + 2; ++i) {
        double x = (i - width / (double) 2.0 + gb->xOffset + 0.5) / gb->xScaleInterval;
        setExprX(gb->expr, x);

        for(j = -1; j < height + 2; ++j) {
            double y = (-j + height / (double) 2.0 + gb->yOffset - 0.5) / gb->yScaleInterval;
            if(j != -1 && j != height + 1) {
                gapType = 0;
                gapKits[0] = gapKitsX[j];
            } else
                gapType = -1; // Actually it is 255 as 'byte' type is unsigned

            if(i == 125 && j == 125)
                gapType = 0;

            double diff = evalExpr(gb->expr, y, gapType, gapKits);

            // X DIFF GAP
            if(exprGapsStr[0] && j != -1 && j != height + 1) {
                //printf("X%d Y%d Gap on X\n", i, j);
                if(j > 0 && j <= height)
                    gapsExpr[1][j - 1] |= 1;
                if(j < height)
                    gapsExpr[1][j] |= 1;

                singleGapKit[0] = gapKits[0];
                double diff1 = evalExpr(gb->expr, y, 1, singleGapKit);
                byte xPoint1 = getDPoint(diffs[0][j + 1], diffs[1][j + 1], diff1);
                if(j < height)
                    points[0][j] += xPoint1;
                if(j > 0 && j <= height)
                    points[0][j - 1] += xPoint1;
                if(xPoint1 >= DPOINT_MIDDLE || (diff1 * (diff1 < 0 ? -1 : 1)) <= getComputingError()) {
                    printf("HERE X%d Y%d\n", i, j);
                    crossingX[1][j] += 1;
                }

                diffs[0][j + 1] = NaN;
                diffs[1][j + 1] = evalExpr(gb->expr, y, 2, singleGapKit);
            }

            // Y DIFF GAP
            /*if(exprGapsStr[1] && j >= 1 && j != height + 1) {
                if(j > 0 && j <= height) {
                    gapsExpr[1][j - 1] |= 1;
                    gapsExpr[0][j - 1] |= 1;
                }

                singleGapKit[0] = gapKits[1];
                double diff1 = evalExpr(gb->expr, y, 1, singleGapKit);
                byte yPoint1 = getDPoint(diffs[1][j - 1], diffs[1][j], diff1);
                points[1][j - 1] += yPoint1;
                if(i > 0)
                    points[0][j - 1] += yPoint1;
                if(yPoint1 >= DPOINT_MIDDLE || (diff1 * (diff1 < 0 ? -1 : 1)) <= getComputingError())
                    crossingY[1][j] += 1;

                diffs[1][j - 1] = NaN;
                diffs[1][j] = evalExpr(gb->expr, y, 2, singleGapKit);
            }*/

            // X
            if(i >= 1 && j != -1) {
                byte xPoint = getDPoint(diffs[0][j + 1], diffs[1][j + 1], diff);

                if(j != height + 1 &&
                        (xPoint >= DPOINT_MIDDLE || (diff * (diff < 0 ? -1 : 1)) <= getComputingError())) {
                    if((gapsExpr[0][j - 1] & 1) != 0)
                        crossingX[1][j] += 1;
                    crossingX[2][j] += 1;
                }

                if(j < height)
                    points[0][j] += xPoint;

                if(j > 0 && j <= height) {
                    points[0][j - 1] += xPoint;
                }
            }

            // Y
            if(j >= 1 && i != -1) {
                byte yPoint = getDPoint(diffs[1][j - 1], diffs[1][j], diff);

                if(yPoint >= DPOINT_MIDDLE || (diff * (diff < 0 ? -1 : 1)) <= getComputingError()) {
                    if((gapsExpr[1][j - 2] & 2) != 0)
                        crossingY[1][j] += 1;
                    crossingY[1][j + 1] += 1;
                }

                points[1][j - 1] += yPoint;
                if(i > 0) {
                    points[0][j - 1] += yPoint;

                    // Now we've done with this point
                    if(points[0][j - 1] >= DPOINT_STRONG) {
                        result[(i - 1) * height + j - 1] = GPOINT_YES;
                        //printf("X%d Y%d\n", i, j);
                    }
                }
            }

            // X GAPS
            if(j > 0 && j <= height && i >= 2 &&
                    gapsExpr[0][j - 1] && result[(i - 2) * height + j - 1] == GPOINT_YES) {
                //printf("X%d Y%d Gap process on X. SumLeft = %d, SumRight = %d\n", i, j, sumLeft, sumRight);
                //printf("X%d Y%d Gap process on X: ", i, j);
                byte gapResult = processGap(gapsExpr[0][j - 1], crossingX[0][j - 1], crossingX[1][j - 1],
                        crossingX[0][j], crossingX[1][j]);
                if(gapResult == 1) {
                    result[(i - 2) * height + j - 1] = GPOINT_GAP;
                    //printf(" success");
                } else if(gapResult == 2)
                    gapsSupported = 0;
                //printf("\n");
            }

            // Y GAPS
            if(j > 1 && j <= height && i >= 2 &&
                    gapsExpr[1][j - 2] && result[(i - 1) * height + j - 2] == GPOINT_YES) {
                //printf("X%d Y%d Gap process on Y. SumTop = %d, SumBottom = %d\n", i, j, sumTop, sumBottom);
                //printf("X%d Y%d Gap process on Y: ", i, j);
                byte gapResult = processGap(gapsExpr[1][j - 2], crossingY[0][j - 1], crossingY[1][j - 1],
                        crossingY[0][j], crossingY[1][j]);
                if(gapResult == 1) {
                    result[(i - 1) * height + j - 2] = GPOINT_GAP;
                    //printf("X%d Y%d Gap process on Y success\n", i, j);
                    //printf(" success");
                } else if(gapResult == 2)
                    gapsSupported = 0;
                //printf("\n");
            }


            if(j < height + 1) {
                diffs[0][j + 1] = diffs[1][j + 1];
                diffs[1][j + 1] = diff;
            }

            if(j < height) {
                points[0][j + 1] = points[1][j + 1];
                points[1][j + 1] = 0;
            }

            if(j > 1) {
                crossingX[0][j - 2] = crossingX[1][j - 2];
                crossingX[1][j - 2] = crossingX[2][j - 2];
                crossingX[2][j - 2] = 0;

                crossingY[0][j - 2] = crossingY[1][j - 2];
                crossingY[1][j - 2] = 0;
            }

            if(j + 1 < height) {
                gapsExpr[0][j + 1] = gapsExpr[1][j + 1];
                gapsExpr[1][j + 1] = 0;
            }


            exprGapsStr[0] = exprGapsStr[1] = 0; // Clearing
        }

        crossingX[0][height] = crossingX[1][height];
        crossingX[1][height] = crossingX[2][height];
        crossingX[2][height] = 0;
    }

    for(i = 0; i < 2; ++i)
        free(diffs[i]);
    free(diffs);
    for(i = 0; i < 2; ++i)
        free(points[i]);
    free(points);

    for(i = 0; i < height + 1; ++i)
        freeGapKit(gapKitsX[i]);
    freeGapKit(gapKitY);
    for(i = 0; i < 2; ++i)
        free(gapKitsX[i]);
    free(gapKitsX);

    for(i = 0; i < 3; ++i)
        free(crossingX[i]);
    free(crossingX);
    for(i = 0; i < 2; ++i)
        free(crossingY[i]);
    free(crossingY);
    free(gapsExpr);

    printf("Gaps supported = %d\n", gapsSupported);

    return result;
}

byte processGap(byte gapExpr, byte topLeft, byte topRight, byte botLeft, byte botRight)
{
    byte result = 0;
    if((gapExpr & 1) != 0) { // X GAP
        int sumLeft = topLeft + botLeft;
        int sumRight = topRight + botRight;
        //printf("sumLeft = %d, sumRight = %d", sumLeft, sumRight);
        if(sumLeft == sumRight && sumLeft == 1) {
            result = 1;
        } else if(sumLeft + sumRight > 2)
            result = 2;
    }
    if((gapExpr & 2) != 0) { // Y GAP
        int sumTop = topLeft + topRight;
        int sumBottom = botLeft + botRight;
        //printf("sumTop = %d, sumBot = %d", sumTop, sumBottom);
        if(sumTop == sumBottom && sumTop == 1 && result != 2) {
            result = 1;
        } else if(sumTop + sumBottom > 2)
            result = 2;
    }
    return result;
}


/*
 *                DIFFS                             POINTS
 *  +---+---+---+---*---*---+---+---+  +---+---+---+---+---+---+---+---+
 *  |   |   |   |   |   |   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+0--+---+---*---*---+--M+---+  +---+1--+---+---+---+---+--M+---+
 *  |   0   1   2   3   4   5   M   7  |   1   |   |   |   |   |   M   |
 *  +---+1--+---+---+---*---+---+---+  +---+---0---+---+---+---+---+---+
 *  |   |   |   |   |   |   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+2--+---+-o-+---+---+---+---+  +---+---+---0-o-+---+---+---+---+
 *  |   |   |   X   |   |   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+3--+-x-+-o-+---+---+---+---+  +---+---+---+-o-0---+---+---+---+
 *  |   ^   ^   ^   ^   ^   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+4--+-x-+-o-+-x-+---+---+---+  +---+---+---+---+---0---+---+---+
 *  |   |   |   X   |   |   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+5--+---+-o-+---+---+---+---+  +---+---+---+---+---+---+---+---+
 *  |   N   |   |   |   |   |   E   |  |   N   |   |   |   |   |   E   |
 *  +---+N--+---+---+---+---+--E+---+  +---+N--+---+---+---+---+--E+---+
 *  |   |   |   |   |   |   |   |   |  |   |   |   |   |   |   |   |   |
 *  +---+7--+---*---*---+---+---+---+  +---+---+---+---+---+---+---+---+
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
