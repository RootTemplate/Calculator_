#ifndef MOREMATH_H
#define MOREMATH_H

#define PI 3.1415926535897932384626433832795

static const double POSITIVE_INFINITY = ((double) 1) / ((double) 0);
static const double NEGATIVE_INFINITY = ((double) -1) / ((double) 0);
static const double NaN = ((double) 0) / ((double) 0);

double lgamma(double x);
double factorial(double x);

double sin1(double x);
double cos1(double x);
double tan1(double x);

double root(double x, long n);

static inline double toRadians(double x) {
    return PI * x / ((double) 180);
}
static inline double toDegrees(double x) {
    return x * ((double) 180) / PI;
}

#endif // MOREMATH_H
