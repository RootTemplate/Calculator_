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

package roottemplate.calculator.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import roottemplate.calculator.PreferencesManager;
import roottemplate.calculator.R;

public class KeyboardButtonsThemeUtils {
    public int[] mBackgroundDrawableIds;
    public int[] mTextColors;

    public KeyboardButtonsThemeUtils(Context context, PreferencesManager prefs) {
        int[] attrs = new int[] {R.attr.styleEqualsButton, R.attr.styleBaseButton,
                R.attr.styleBaseButton, R.attr.styleSymbolButton};
        mBackgroundDrawableIds = new int[attrs.length];
        mTextColors = new int[attrs.length];

        TypedArray ta = context.obtainStyledAttributes(attrs);
        int[] attrs1 = new int[] {android.R.attr.textColor, android.R.attr.background, };
        for(int i = 0; i < attrs.length; i++) {
            int style = ta.getResourceId(i, 0);
            TypedArray ta1 = context.obtainStyledAttributes(style, attrs1);
            mBackgroundDrawableIds[i] = ta1.getResourceId(ta1.getIndex(1), 0);
            mTextColors[i] = ta1.getColor(ta1.getIndex(0), 0);
            ta1.recycle();
        }
        ta.recycle();
    }
}
