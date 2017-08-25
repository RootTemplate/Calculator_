package roottemplate.calculator.evaluator.util;

import java.util.Locale;

public class Util {
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    
    /**
     * Converts double to string.
     * @param value The number
     * @param minExponent if value is less then this, then it's guaranteed no
     * exponent will be in the result
     * @param precision the precision
     * @return result of converting
     */
    public static String doubleToString(double value, int minExponent, int precision) {
        // Source: android's calculator. CalculatorDisplay class.
        if(Double.isNaN(value) || Double.isInfinite(value))
            return String.valueOf(value);
        
        String method;
        if(Math.pow(10, minExponent) > value) {
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
    }
}
