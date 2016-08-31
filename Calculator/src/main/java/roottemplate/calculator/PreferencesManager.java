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

import android.content.SharedPreferences;
import android.content.res.Resources;

public class PreferencesManager {
    private final SharedPreferences mPrefs;
    private final Resources mResources;
    public PreferencesManager(SharedPreferences prefs, Resources resources) {
        mPrefs = prefs;
        mResources = resources;
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

    public int bracketClosingType() {
        return Integer.parseInt(mPrefs.getString(
                "bracketClosingType", mResources.getString(R.string.pref_def_brClosingType)));
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

    public boolean enabledTips() {
        return mPrefs.getBoolean("enabledTips", mResources.getBoolean(R.bool.pref_def_enabledTips));
    }
}
