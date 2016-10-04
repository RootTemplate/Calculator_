#ifndef GRAPHEREXPRESSIONOPCODES_H
#define GRAPHEREXPRESSIONOPCODES_H

// EOP = Expression OPcode

// EOP for 2 args
#define EOP_ADD -1
#define EOP_SUB -2
#define EOP_MUL -3
#define EOP_DIV -4
#define EOP_REM -5
#define EOP_POW -6
#define EOP_ROOT -7
                    // Args: v, n. n-th root of number v

// EOP for 1 arg
#define EOP_NEG -8
#define EOP_FACT -9

#define EOP_ROUND -10
#define EOP_FLOOR -11
#define EOP_CEIL -12
#define EOP_TORAD (-12 - 1)
#define EOP_TODEG -14
#define EOP_LOG10 -15
#define EOP_LOG -16
#define EOP_ABS -17
#define EOP_SQRT -18
#define EOP_CBRT -19

#define EOP_SIN -20
#define EOP_COS -21
#define EOP_TAN -22
#define EOP_ASIN -23
#define EOP_ACOS -24
#define EOP_ATAN -25

#endif // GRAPHEREXPRESSIONOPCODES_H
