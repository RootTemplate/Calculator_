/*
 * Copyright 2016-2017 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
 * Calculator_ Engine (Evaluator) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ Engine (Evaluator) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_ Engine (Evaluator).  If not, see <http://www.gnu.org/licenses/>.
 */

/*
    Some methods below were taken from:
        Arity, a java library by Javia, which is licensed under Apache License v. 2.0
    The methods are:
        - asinh, acosh, atanh, gcd, lgamma, factorial, isPiMultiple
    And field:
        - GAMMA, FACT
    Some methods were also modified:
        - sin, cos, tan

    Previous class location of MoreMath:
        org.javia.arity.MoreMath
        http://javia.org
    Source code:
        https://github.com/Xlythe/Arity/blob/master/src/org/javia/arity/MoreMath.java

    The copyright of the library is presented below.
 */

/*
 * Copyright (C) 2008-2009 Mihai Preda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package roottemplate.calculator.evaluator.util;

public class MoreMath {
    public static final double LOG10 = Math.log(10);

    public static final double asinh(double x) {
        return (x < 0) ? -asinh(-x) : Math.log(x + x + 1/(Math.sqrt(x*x + 1) + x));
    }

    public static final double acosh(double x) {
        return Math.log(x + x - 1/(Math.sqrt(x*x - 1) + x));
    }

    public static final double atanh(double x) {
        return (x < 0) ? -atanh(-x) : 0.5 * Math.log(1. + (x + x)/(1 - x));
    }

    public static final double gcd(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y) ||
                Double.isInfinite(x) || Double.isInfinite(y)) {
            return Double.NaN;
        }
        x = Math.abs(x);
        y = Math.abs(y);
        while (x < y * 1e15) {
            final double save = y;
            y = x % y;
            x = save;
        }
        return x;
    }

    public static final double GAMMA[] = {
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
 
    public static double lgamma(double x) {
        double tmp = x + 5.2421875; //== 607/128. + .5;
        double sum = 0.99999999999999709182;
        for (int i = 0; i < GAMMA.length; ++i) {
            sum += GAMMA[i] / ++x;
        }

        return 0.9189385332046727418 //LN_SQRT2PI, ln(sqrt(2*pi))
            + Math.log(sum)
            + (tmp-4.7421875)*Math.log(tmp) - tmp
            ;
    }

    static final double FACT[] = {
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

    public static double factorial(double x) {
        if (x < 0) { // x <= -1 ?
            return Double.NaN;
        }
        if (x <= 170) {
            if (Math.floor(x) == x) {
                int n = (int)x;
                double extra = x;
                switch (n & 7) {
                case 7: extra *= --x;
                case 6: extra *= --x;
                case 5: extra *= --x;
                case 4: extra *= --x;
                case 3: extra *= --x;
                case 2: extra *= --x;
                case 1: return FACT[n >> 3] * extra;
                case 0: return FACT[n >> 3];
                }
            }
        }
        return Math.exp(lgamma(x));
    }
    
    private static boolean isPiMultiple(double x) {
        // x % y == 0
        final double d = x / Math.PI;
        return d == Math.floor(d);
    }

    public static double sin(double x) {
        return isPiMultiple(x) ? 0 : Math.sin(x);
    }

    public static double cos(double x) {
        return isPiMultiple(x-Math.PI/2) ? 0 : Math.cos(x);
    }

    public static double tan(double x) {
        if(isPiMultiple(x)) return 0;
        if(isPiMultiple(x-Math.PI/2))
            return x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        return Math.tan(x);
    }

    public static double root(double number, long of) {
        if(of == 2)
            return Math.sqrt(number);
        if(of == 3)
            return Math.cbrt(number);
        if(number < 0) {
            if(of % 2 == 0)
                return Double.NaN;
            return -Math.pow(-number, 1 / (double) of);
        } else
            return Math.pow(number, 1 / (double) of);
    }

    public static double log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }
}
