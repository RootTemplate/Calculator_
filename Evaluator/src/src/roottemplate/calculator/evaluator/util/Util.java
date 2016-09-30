/* 
 * Copyright 2016 RootTemplate Group 1
 *
 * This file is part of Calculator_ Engine (Evaluator).
 *
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
package roottemplate.calculator.evaluator.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Util {
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    
    
    /* THE CODE BELOW HAS BEEN TAKEN FROM Arity PROJECT BY Javia */
    /* PREVIOUS CLASS LOCATION: org.javia.arity.Util. License: Apache License v. 2 */
    /* Javia's Copyright (C) 2007-2008 Mihai Preda. */
    /* http://javia.org */
    /*
    
    public static final int FLOAT_PRECISION  = -1;
    
    /**
      Returns an approximation with no more than maxLen chars.

      This method is not public, it is called through doubleToString, 
      that's why we can make some assumptions about the format of the string,
      such as assuming that the exponent 'E' is upper-case.

      @param str the value to truncate (e.g. "-2.898983455E20")
      @param maxLen the maximum number of characters in the returned string
      @return a truncation no longer then maxLen (e.g. "-2.8E20" for maxLen=7).
     * /
    static String sizeTruncate(String str, int maxLen) {
        /*if (maxLen == LEN_UNLIMITED) {
            return str;
        }* /
        int ePos = str.lastIndexOf('E');
        String tail = (ePos != -1) ? str.substring(ePos) : "";
        int tailLen = tail.length();
        int headLen = str.length() - tailLen;
        int maxHeadLen = maxLen - tailLen;
        int keepLen = Math.min(headLen, maxHeadLen);
        if (keepLen < 1 || (keepLen < 2 && str.length() > 0 && str.charAt(0) == '-')) {
            return str; // impossible to truncate
        }
        int dotPos = str.indexOf('.');
        if (dotPos == -1) {
            dotPos = headLen;
        }
        if (dotPos > keepLen) {
            int exponent = (ePos != -1) ? Integer.parseInt(str.substring(ePos + 1)) : 0;
            int start = str.charAt(0) == '-' ? 1 : 0;
            exponent += dotPos - start - 1;
            String newStr = str.substring(0, start+1) + '.' + str.substring(start+1, headLen) + 'E' + exponent;
            return sizeTruncate(newStr, maxLen);

        }
        return str.substring(0, keepLen) + tail;
    }

    /**
       Rounds by dropping roundingDigits of double precision 
       (similar to 'hidden precision digits' on calculators),
       and formats to String.
       @param v the value to be converted to String
       @param roundingDigits the number of 'hidden precision' digits (e.g. 2).
       @return a String representation of v
     * /
    public static String doubleToString(final double v, final int roundingDigits) {
        final double absv = Math.abs(v);
        final String str = roundingDigits == FLOAT_PRECISION ? Float.toString((float) absv) : Double.toString(absv);
        StringBuffer buf = new StringBuffer(str);
        int roundingStart = (roundingDigits <= 0 || roundingDigits > 13) ? 17 : (16 - roundingDigits);

        int ePos = str.lastIndexOf('E');
        int exp  =  (ePos != -1) ? Integer.parseInt(str.substring(ePos + 1)) : 0;
        if (ePos != -1) {
            buf.setLength(ePos);
        }
        int len = buf.length();

        //remove dot
        int dotPos;
        for (dotPos = 0; dotPos < len && buf.charAt(dotPos) != '.';) {
            ++dotPos;
        }
        exp += dotPos;
        if (dotPos < len) {
            buf.deleteCharAt(dotPos);
            --len;
        }

        //round
        for (int p = 0; p < len && buf.charAt(p) == '0'; ++p) { 
            ++roundingStart; 
        }

        if (roundingStart < len) {
            if (buf.charAt(roundingStart) >= '5') {
                int p;
                for (p = roundingStart-1; p >= 0 && buf.charAt(p)=='9'; --p) {
                    buf.setCharAt(p, '0');
                }
                if (p >= 0) {
                    buf.setCharAt(p, (char)(buf.charAt(p)+1));
                } else {
                    buf.insert(0, '1');
                    ++roundingStart;
                    ++exp;
                }
            }
            buf.setLength(roundingStart);
        }

        //re-insert dot
        if ((exp < -5) || (exp > 10)) {
            buf.insert(1, '.');
            --exp;
        } else {
            for (int i = len; i < exp; ++i) {
                buf.append('0');
            }
            for (int i = exp; i <= 0; ++i) {
                buf.insert(0, '0');
            }
            buf.insert((exp<=0)? 1 : exp, '.');
            exp = 0;
        }
        len = buf.length();
        
        //remove trailing dot and 0s.
        int tail;
        for (tail = len-1; tail >= 0 && buf.charAt(tail) == '0'; --tail) {
            buf.deleteCharAt(tail);
        }
        if (tail >= 0 && buf.charAt(tail) == '.') {
            buf.deleteCharAt(tail);
        }

        if (exp != 0) {
            buf.append('E').append(exp);
        }
        if (v < 0) {
            buf.insert(0, '-');
        }
        return buf.toString();
    }

    /**
       Renders a real number to a String (for user display).
       @param maxLen the maximum total length of the resulting string
       @param rounding the number of final digits to round
     * /
    public static String doubleToString(double x, int maxLen, int rounding) {
        return sizeTruncate(doubleToString(x, rounding), maxLen);
    }
    
    /* THE CODE ABOVE HAS BEEN TAKEN FROM Arity PROJECT BY Javia */
    /* FOR MORE INFORMATION SEE ABOVE */
    
    //public static String doubleToStringWithTruncating(double x, int maxLen) {
    //    return doubleToString(x, maxLen, -2);
    //}
    
    public static String doubleToString(double n, int maxLen, int maxNonEDigits, int minNonEDigits) {
        if(Double.isNaN(n) || Double.isInfinite(n))
            return String.valueOf(n);
        
        double absN = Math.abs(n);
        boolean withE = (absN >= 1 || absN == 0) ? absN > Math.pow(10, maxNonEDigits) : absN < Math.pow(10, -minNonEDigits);
        
        return withE ? doubleToStringWithE(n, maxLen) : doubleToStringNoE(n, maxLen);
        
        /*String result;
        String method = withE ? "g" : "f";
        
        for(int i = maxLen; i > maxLen - timesTrying; i--) {
            result = String.format(Locale.US, "%." + i + method, n);
            String mantissa = result;
            String exponent = null;
            int e = result.indexOf('e');
            if (e != -1) {
                mantissa = result.substring(0, e);
                // Strip "+" and unnecessary 0's from the exponent
                exponent = result.substring(e + 1);
                exponent = String.valueOf(Integer.parseInt(exponent));
            }

            //mantissa = mantissa.replaceAll(",", ".");
            int period = mantissa.indexOf('.');
            if (period != -1) {
                // Strip trailing 0's
                while (mantissa.length() > 0 && mantissa.endsWith("0")) {
                    mantissa = mantissa.substring(0, mantissa.length() - 1);
                }
                if (mantissa.length() == period + 1) {
                    mantissa = mantissa.substring(0, mantissa.length() - 1);
                }
            }

            if (exponent != null) {
                result = mantissa + 'E' + exponent;
            } else {
                result = mantissa;
            }
            if(result.length() <= maxLen || i == 0)
                return result;
        }
        
        return null;*/
    }
    
    public static String doubleToStringWithE(double n, int maxLen) {
        String str = String.format(Locale.US, "%.340e", n);
        int eIndex = str.indexOf('e');
        String mantissa = str.substring(0, eIndex);
        
        String exponent = str.substring(eIndex + 1);
        if(exponent.startsWith("+")) exponent = exponent.substring(1);
        int exponentV = Integer.parseInt(exponent);
        exponent = String.valueOf(exponentV);
        
        StringBuilder result = new StringBuilder(mantissa);
        for(int i = 0; i < 2 && result.length() + 1 + exponent.length() > maxLen; i++) {
            int appended = truncate(result, maxLen - 1 - exponent.length(), mantissa.indexOf('.'));
            
            if(appended > 0) { // Which can be only if result was rounded up to point ('.'), so there cannot be any point
                int j = result.length() - 1;
                while(j > 0 && result.charAt(j) == '0') {
                    result.deleteCharAt(j);
                    exponentV++;
                    j--;
                }
            }
            exponent = String.valueOf(exponentV);
        }
        
        return result.append('E').append(exponent).toString();
    }
    
    public static String doubleToStringNoE(double n, int maxLen) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));
        df.setMaximumFractionDigits(340);
        String str = df.format(n);
        
        int point = str.indexOf('.');
        if(point == -1 && str.length() > maxLen ||
                point != -1 && point > maxLen)
            return doubleToStringWithE(n, maxLen); // Looks like wrong params were given: impossible to fit maxLen
        
        StringBuilder result = new StringBuilder(str);
        truncate(result, maxLen, point);
        
        if(result.length() > maxLen)
            return doubleToStringWithE(n, maxLen);
        else
            return result.toString();
    }
    
    private static int truncate(StringBuilder result, int maxLen, int point) {
        int digitsAppended = 0;
        int digitCuttable = point == -1 ? 0 : (result.length() - point - 1);
        while(digitCuttable >= 0 && result.length() > maxLen) {
            boolean oneUp = false;
            while(digitCuttable >= 0 && result.length() > maxLen) {
                int i = result.length() - 1;
                oneUp = result.charAt(i) >= '5';
                result.deleteCharAt(i);
                digitCuttable--;
            }
            
            int i = result.length() - 1;
            while(oneUp && i >= 0) {
                char c = result.charAt(i);
                if(c != '.') {
                    if(c == '9') {
                        if(digitCuttable > 0)
                            result.deleteCharAt(i);
                        else
                            result.setCharAt(i, '0');
                        oneUp = true;
                    } else {
                        result.setCharAt(i, ++c);
                        oneUp = false;
                    }
                } else
                    result.deleteCharAt(i);
                i--;
                digitCuttable--;
            }
            
            i = result.length() - 1;
            while(i >= 1 && digitCuttable > 0) {
                char c = result.charAt(i);
                if(c == '0') {
                    result.deleteCharAt(i);
                    digitCuttable--;
                    i--;
                } else
                    break;
            }
            if(result.charAt(result.length() - 1) == '.')
                result.deleteCharAt(result.length() - 1);
            
            if(oneUp) {
                result.insert(0, '1');
                digitsAppended++;
            }
        }
        return digitsAppended;
    }
    
    /*public static String doubleToString(double n, int maxLen) {
        return doubleToString(n, maxLen, 7, 7, maxLen);
    }*/
    
    /* *
     * Converts double to string.
     * @param value The number
     * @param minExponent if value is less then this, then it's guaranteed no
     * exponent will be in the result
     * @param precision the precision
     * @return result of converting
     * /
    public static String doubleToString(double value, int minExponent, int precision) {
        // Source: android's calculator. CalculatorDisplay class.
        if(Double.isNaN(value) || Double.isInfinite(value))
            return String.valueOf(value);
        
        String method;
        if(Math.pow(10, minExponent) > Math.abs(value)) {
            method = "f";
        } else
            method = "g";
            
        String result = String.format(Locale.US, "%" + "." + precision + method, value);
        String mantissa = result;
        String exponent = null;
        int e = result.indexOf('e');
        if (e != -1) {
            mantissa = result.substring(0, e);
            // Strip "+" and unnecessary 0's from the exponent
            exponent = result.substring(e + 1);
            if (exponent.startsWith("+")) {
                exponent = exponent.substring(1);
            }
            exponent = String.valueOf(Integer.parseInt(exponent));
        }
        
        //mantissa = mantissa.replaceAll(",", ".");
        int period = mantissa.indexOf('.');
        if (period != -1) {
            // Strip trailing 0's
            while (mantissa.length() > 0 && mantissa.endsWith("0")) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
            if (mantissa.length() == period + 1) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
        }
        
        if (exponent != null) {
            result = mantissa + 'E' + exponent;
        } else {
            result = mantissa;
        }
        return result;
    }
    public static String doubleToString(double value, int precision) {
        return doubleToString(value, 10, precision);
    }
    public static String doubleToString(double value) {
        return doubleToString(value, 15);
    }*/
}
