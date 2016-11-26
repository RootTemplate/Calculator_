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
    
    public static String doubleToString(double n, int maxLen, int maxNonEDigits, int minNonEDigits) {
        if(Double.isNaN(n) || Double.isInfinite(n))
            return String.valueOf(n);
        
        double absN = Math.abs(n);
        boolean withE = (absN >= 1 || absN == 0) ? absN > Math.pow(10, maxNonEDigits) : absN < Math.pow(10, -minNonEDigits);
        
        return withE ? doubleToStringWithE(n, maxLen) : doubleToStringNoE(n, maxLen);
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
}
