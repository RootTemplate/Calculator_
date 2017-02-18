/*
 * Copyright (c) 2017 RootTemplate Group 1.
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

package roottemplate.calculator.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import roottemplate.calculator.MainActivity;
import roottemplate.calculator.PreferencesManager;
import roottemplate.calculator.R;

public class SystemButton extends Button {
    /* THERE IS ONLY ONE PROPERTY -- MEASUREMENT UNITS of ANGLE
    private static final int PROPERTY_AMU = 1;
    private int mProperty;
    */

    public SystemButton(Context context) {
        super(context);
    }

    public SystemButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SystemButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SystemButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initButton(String propertyName) {
        /*if(propertyName.equals("amu"))
            mProperty = PROPERTY_AMU;*/
        updateText(new PreferencesManager(getContext()).getAMU());
    }

    public int getProperty() {
        // return mProperty;
        return 1;
    }

    public void onButtonClicked(MainActivity activity) {
        PreferencesManager prefs = activity.getPrefs();
        int amu = prefs.getAMU() + 1;
        if(amu > 2) amu = 1;
        prefs.amu(amu);

        activity.invalidateEvaluatorOptions();
        updateText(amu);
    }

    private void updateText(int amu) {
        switch(amu) {
            case 1: setText(R.string.rad); break;
            case 2: setText(R.string.deg); break;
        }
    }
}
