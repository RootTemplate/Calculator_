#include "MoreMath.h"
#include <math.h>

const double GAMMA[] = {
        57.156235665862923517,
        -59.597960355475491248,
        14.136097974741747174,
        -0.49191381609762019978,
        .33994649984811888699e-4,
        .46523628927048575665e-4,
        -.98374475304879564677e-4,
        .15808870322491248884e-3,
        -.21026444172410488319e-3,
        .21743961811521264320e-3,
        -.16431810653676389022e-3,
        .84418223983852743293e-4,
        -.26190838401581408670e-4,
        .36899182659531622704e-5
};
const int GAMMA_LENGTH = 14;

double lgamma(double x) {
    int i;
    double tmp = x + 5.2421875; //== 607/128. + .5;
    double sum = 0.99999999999999709182;
    for (i = 0; i < GAMMA_LENGTH; ++i) {
        sum += GAMMA[i] / ++x;
    }

    return 0.9189385332046727418 //LN_SQRT2PI, ln(sqrt(2*pi))
           + log(sum)
           + (tmp - 4.7421875) * log(tmp) - tmp;
}

const double FACT[] = {
        1.0,
        40320.0,
        2.0922789888E13,
        6.204484017332394E23,
        2.631308369336935E35,
        8.159152832478977E47,
        1.2413915592536073E61,
        7.109985878048635E74,
        1.2688693218588417E89,
        6.1234458376886085E103,
        7.156945704626381E118,
        1.8548264225739844E134,
        9.916779348709496E149,
        1.0299016745145628E166,
        1.974506857221074E182,
        6.689502913449127E198,
        3.856204823625804E215,
        3.659042881952549E232,
        5.5502938327393044E249,
        1.3113358856834524E267,
        4.7147236359920616E284,
        2.5260757449731984E302,
};

double factorial(double x) {
    if (x < 0) { // x <= -1 ?
        return NaN;
    }
    if (x <= 170) {
        if (floor(x) == x) {
            int n = (int) x;
            double extra = x;
            switch (n & 7) {
                case 7: {
                    extra *= --x;
                }
                case 6: {
                    extra *= --x;
                }
                case 5: {
                    extra *= --x;
                }
                case 4: {
                    extra *= --x;
                }
                case 3: {
                    extra *= --x;
                }
                case 2: {
                    extra *= --x;
                }
                case 1: {
                    return FACT[n >> 3] * extra;
                }
                case 0: {
                    return FACT[n >> 3] * 1;
                }
            }
        }
    }
    return exp(lgamma(x));
}

int isPiMultiple(double x) {
    // x % y == 0
    double d = x / PI;
    if(d == floor(d))
        return 1;
    return 0;
}

double sin1(double x) {
    return isPiMultiple(x) ? 0 : sin(x);
}

double cos1(double x) {
    return isPiMultiple(x - PI / 2) ? 0 : cos(x);
}

double tan1(double x) {
    if (isPiMultiple(x)) return (double) 0;
    if (isPiMultiple(x - PI / 2)) {
        if(x >= 0)
            return POSITIVE_INFINITY;
        return NEGATIVE_INFINITY;
    }
    return tan(x);
}

double root(double number, long round) {
    if (round == 2)
        return sqrt(number);
    if (round == 3)
        return cbrt(number);
    if (number < 0) {
        if (round % 2 == 0)
            return NaN;
        return -pow(-number, 1 / (double) round);
    } else
        return pow(number, 1 / (double) round);
}
