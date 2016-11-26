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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;

import java.util.Locale;
import java.util.Objects;

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

    public static String intToHexColor(int color) {
        return "#" + Integer.toHexString(color & 0xffffff);
    }

    public static String inverseTextCase(String str) {
        String upper = str.toUpperCase();
        return upper.equals(str) ? str.toLowerCase() : upper;
    }

    public static long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }



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
}
