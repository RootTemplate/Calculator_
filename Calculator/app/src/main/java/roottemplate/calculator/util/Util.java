/*
 * Copyright (c) 2016 RootTemplate Group 1.
 * This file is part of Calculator_.
 *
 * Calculator_ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Calculator_ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Calculator_.  If not, see <http://www.gnu.org/licenses/>.
 */

package roottemplate.calculator.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import roottemplate.calculator.R;
import roottemplate.calculator.data.KeyboardKits;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.view.FatalErrorDialogFragment;

public class Util {
    public static final String LOG_TAG = "Calculator_";
    public static final String AUTHOR_WEBSITE = "http://roottemplate.pe.hu";

    public static void setPrimaryClip(Context context, String clip) {
        ClipboardManager clipboard = (ClipboardManager) context.
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(clip);
    }

    public static String getPrimaryClip(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.
                getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence text = clipboard.getText();
        return (text == null) ? "" : text.toString();
    }

    public static boolean isWestLocale(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String country = locale.getCountry();
        if(country.equals("US") || country.equals("CA"))
            return true;
        if(country.isEmpty()) {
            String lang = locale.getLanguage();
            if(lang.equals("en"))
                return true;
        }
        return false;
    }

    public static String doubleToString(double x) {
        String result = Double.toString(x);
        if(result.endsWith(".0")) result = result.substring(0, result.length() - 2);
        return result;
    }

    public static String intToHexColor(int color) {
        return "#" + Integer.toHexString(color & 0xffffff);
    }

    public static int[][] cloneIntMatrix(int[][] m) {
        int[][] res = new int[m.length][];
        for(int i = 0; i < m.length; i++) {
            int[] cache = m[i];
            res[i] = new int[cache.length];
            System.arraycopy(cache, 0, res[i], 0, cache.length);
        }
        return res;
    }

    public static String inverseTextCase(String str) {
        String t = str.toUpperCase();
        t = t.equals(str) ? str.toLowerCase() : t;
        char[] charArray = t.toCharArray();
        for(int i = 0; i < charArray.length; i++) {
            char setTo;
            switch (charArray[i]) {
                case '(': setTo = ')'; break;
                case ')': setTo = '('; break;
                case '[': setTo = ']'; break;
                case ']': setTo = '['; break;
                case '{': setTo = '}'; break;
                case '}': setTo = '{'; break;
                case '<': setTo = '>'; break;
                case '>': setTo = '<'; break;
                default:
                    continue; // continue 'for' loop
            }
            charArray[i] = setTo;
        }
        return new String(charArray);
    }

    public static long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int[] appendToIntArray(int[] a, int index, int value) {
        int[] res = new int[a.length + 1];
        System.arraycopy(a, 0, res, 0, index);
        res[index] = value;
        System.arraycopy(a, index, res, index + 1, a.length - index);
        return res;
    }
    public static <T> T[] appendToObjectArray(T[] a, int index, T value, T[] newA) {
        System.arraycopy(a, 0, newA, 0, index);
        newA[index] = value;
        System.arraycopy(a, index, newA, index + 1, a.length - index);
        return newA;
    }
    public static int[] removeFromIntArray(int[] a, int index) {
        int[] res = new int[a.length - 1];
        System.arraycopy(a, 0, res, 0, index);
        System.arraycopy(a, index + 1, res, index, a.length - index - 1);
        return res;
    }
    public static <T> T[] removeFromObjectArray(T[] a, int index, T[] newA) {
        System.arraycopy(a, 0, newA, 0, index);
        System.arraycopy(a, index + 1, newA, index, a.length - index - 1);
        return newA;
    }


    /** APP DEPENDENT **/

    public static void fatalError(final AppCompatActivity activity, int dialogMessage, Exception e) {
        Bundle args = new Bundle();
        if(e == null)
            args.putString("message", activity.getResources().getString(dialogMessage));
        else {
            args.putString("message", String.format(activity.getResources().getString(dialogMessage), e.getMessage()));
        }
        FatalErrorDialogFragment df = new FatalErrorDialogFragment();
        df.setArguments(args);
        df.show(activity.getSupportFragmentManager(), FatalErrorDialogFragment.FRAGMENT_TAG);
    }

    public static int getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Util.LOG_TAG, "Unable to get version", e);
            return -1;
        }
    }

    public static int twilightManager_isNight(Context context) {
        try {
            Class c = Class.forName("android.support.v7.app.TwilightManager");
            Method inst = c.getDeclaredMethod("getInstance", Context.class);
            inst.setAccessible(true);
            Object instance = inst.invoke(null, context);
            Method m = c.getDeclaredMethod("isNight");
            m.setAccessible(true);
            return ((boolean) m.invoke(instance)) ? 1 : 0;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to call android.support.v7.app.TwilightManager.isNight()", e);
            return -1;
        }
    }

    public static KeyboardKits readKeyboardKits(AppCompatActivity activity) {
        KeyboardKits kits = null;
        try {
            kits = KeyboardKitsXmlManager.parse(activity);
            if (kits.mKits.size() == 0) {
                Log.e(Util.LOG_TAG, "ButtonKits xml file has 0 kits. Restoring default xml");
                KeyboardKitsXmlManager.restoreDefaultButtonKitsXml(activity);
                kits = KeyboardKitsXmlManager.parse(activity);
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(Util.LOG_TAG, "Exception while parsing ButtonKits", e);
            Util.fatalError(activity, R.string.message_bad_kits_xml, e);
        }
        return kits;
    }

    public static void animateAlpha(View v, int alpha, int duration) {
        if(Build.VERSION.SDK_INT >= 12) {
            v.animate().alpha(alpha).setDuration(duration);
        } else
            v.setAlpha(alpha);
    }
}
