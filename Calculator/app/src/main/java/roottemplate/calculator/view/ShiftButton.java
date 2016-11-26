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

package roottemplate.calculator.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import roottemplate.calculator.R;

public class ShiftButton extends ImageButton {
    private static final int STATE_UNSET = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_CAPSLOCK = 2;

    public static boolean isInverseEnabled(int shiftState) {
        return shiftState != STATE_DISABLED;
    }

    private int mState = STATE_UNSET;

    public ShiftButton(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShiftButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShiftButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShiftButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView() {
        setState(STATE_DISABLED);
    }

    public void setState(int state) {
        if(state == mState) return;

        mState = state;
        switch (state) {
            case STATE_DISABLED:
                setImageDrawable(getResources().getDrawable(R.drawable.ic_shift_disabled));
                break;
            case STATE_ENABLED:
                setImageDrawable(getResources().getDrawable(R.drawable.ic_shift_enabled));
                break;
            case STATE_CAPSLOCK:
                setImageDrawable(getResources().getDrawable(R.drawable.ic_shift_capslock));
                break;
            case STATE_UNSET:
                break;
            default:
                throw new RuntimeException("[ShiftButton] State " + state + " is unknown");
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        if(mState != STATE_DISABLED) {
            SavedState ss = new SavedState(superState);
            ss.mState = mState;
            return ss;
        } else
            return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        setState(ss.mState);
        super.onRestoreInstanceState(ss.getSuperState());

    }

    private static class SavedState extends BaseSavedState {
        private int mState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mState = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
