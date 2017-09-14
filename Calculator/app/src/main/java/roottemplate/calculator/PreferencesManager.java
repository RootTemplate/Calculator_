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

package roottemplate.calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import roottemplate.calculator.util.Util;

public class PreferencesManager {
    private final SharedPreferences mPrefs;
    private final Resources mResources;
    public PreferencesManager(Context c) {
        this(PreferenceManager.getDefaultSharedPreferences(c), c.getResources());
    }
    public PreferencesManager(SharedPreferences prefs, Resources resources) {
        mPrefs = prefs;
        mResources = resources;
    }

    public Resources getResources() {
        return mResources;
    }

    public String kitName() {
        return mPrefs.getString("selectedKit", null);
    }
    public void kitName(String kitName) {
        mPrefs.edit().putString("selectedKit", kitName).apply();
    }

    public int version() {
        return mPrefs.getInt("version", -1);
    }
    public void version(int ver) {
        mPrefs.edit().putInt("version", ver).apply();
    }

    public boolean keyboardsAdvancedSettings() {
        return mPrefs.getBoolean("keyboard_advancedSettings", false);
    }
    public void keyboardsAdvancedSettings(boolean newValue) {
        mPrefs.edit().putBoolean("keyboard_advancedSettings", newValue).apply();
    }

    public int bracketClosingType() {
        return Integer.parseInt(mPrefs.getString(
                "bracketClosingType", mResources.getString(R.string.pref_def_brClosingType)));
    }

    public boolean autoBracketClosing() {
        return mPrefs.getBoolean("autoBracketClosing",
                mResources.getBoolean(R.bool.pref_def_autoBracketClosing));
    }

    public boolean doRound() {
        return mPrefs.getBoolean("doRound", mResources.getBoolean(R.bool.pref_def_doRound));
    }

    public boolean enabledHistory() {
        return mPrefs.getBoolean("enabledHistory", mResources.getBoolean(R.bool.pref_def_enabledHistory));
    }

    public int storingHistory() {
        return Integer.parseInt(mPrefs.getString("storingHistory",
                mResources.getString(R.string.pref_def_storingHistory)));
    }

    public int getAMU() {
        return Integer.parseInt(mPrefs.getString("amu", "2"));
    }
    public void amu(int amu) {
        mPrefs.edit().putString("amu", String.valueOf(amu)).apply();
    }

    public boolean enabledTips() {
        return mPrefs.getBoolean("enabledTips", mResources.getBoolean(R.bool.pref_def_enabledTips));
    }

    public boolean digitGrouping() {
        try {
            return mPrefs.getBoolean("digitGrouping",
                    mResources.getBoolean(R.bool.pref_def_digitGrouping));
        } catch(Exception e) {
            Log.e(Util.LOG_TAG, "While reading digitGrouping key", e);
            // Before build 15 it was a String, not a boolean
            boolean res = Integer.parseInt(mPrefs.getString("digitGrouping", "0")) > 0;
            mPrefs.edit().remove("digitGrouping").putBoolean("digitGrouping", res).apply();
            return res;
        }
    }

    public int digitSeparatorLeft() {
        return Integer.parseInt(mPrefs.getString("digitGroupingSeparatorLeft",
                mResources.getString(R.string.pref_def_digitGrouping_separator_left)));
    }

    public int digitSeparatorFract() {
        return Integer.parseInt(mPrefs.getString("digitGroupingSeparatorFract",
                mResources.getString(R.string.pref_def_digitGrouping_separator_fract)));
    }

    public int storingNamespace() {
        return Integer.parseInt(mPrefs.getString("storingNamespace",
                mResources.getString(R.string.pref_def_storingNamespace)));
    }

    public boolean separateNamespace() {
        return mPrefs.getBoolean("separateNamespace",
                mResources.getBoolean(R.bool.pref_def_separateNamespace));
    }

    public boolean highlightE() {
        return mPrefs.getBoolean("highlightE", mResources.getBoolean(R.bool.pref_def_highlightE));
    }

    public int outputType() {
        return Integer.parseInt(mPrefs.getString("outputType",
                mResources.getString(R.string.pref_def_outputType)));
    }

    public static final int THEME_DAY = 1;
    public static final int THEME_NIGHT = 2;
    public static final int THEME_LEGACY = 3;
    public int dayNightTheme() {
        return Integer.parseInt(mPrefs.getString("dayNightTheme",
                mResources.getString(R.string.pref_def_dayNightTheme)));
    }
    public static int dayNightThemeIdToMode(Context context, int themeId) {
        switch(themeId) {
            case 0: return AppCompatDelegate.MODE_NIGHT_AUTO;
            case 1: return AppCompatDelegate.MODE_NIGHT_NO;
            case 2: return AppCompatDelegate.MODE_NIGHT_YES;
            case 3: return AppCompatDelegate.MODE_NIGHT_YES;
            case -1:
                int result = Util.twilightManager_isNight(context);
                if(result == 1)
                    return AppCompatDelegate.MODE_NIGHT_NO;
                else if(result == 0)
                    return AppCompatDelegate.MODE_NIGHT_YES;
                else
                    return AppCompatDelegate.MODE_NIGHT_AUTO;
        }
        return AppCompatDelegate.MODE_NIGHT_AUTO;
    }
    public int getAppTheme() {
        boolean isDay = (mResources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                != Configuration.UI_MODE_NIGHT_YES;
        if(isDay) return THEME_DAY;
        if(dayNightTheme() == THEME_LEGACY)
            return THEME_LEGACY;
        return THEME_NIGHT;
    }

    public boolean darkOrangeEquals() {
        return mPrefs.getBoolean("darkOrangeEquals", mResources.getBoolean(R.bool.pref_def_darkOrangeEquals));
    }
}
