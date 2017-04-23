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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import roottemplate.calculator.MainActivity;
import roottemplate.calculator.PreferencesManager;
import roottemplate.calculator.R;
import roottemplate.calculator.data.KeyboardKits;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.util.Util;

public class KitViewPager extends ViewPager {
    public KitViewPager(Context context) {
        super(context);
    }

    public KitViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(FragmentManager fm, int pagesLength, int mainPageIndex) {
        setAdapter(new Adapter(fm, pagesLength));
        setCurrentItem(mainPageIndex, false);
    }

    public PageFragment getCurrentPageFragment() {
        return (PageFragment) getAdapter().instantiateItem(this, getCurrentItem());
    }

    private static class Adapter extends FragmentPagerAdapter {
        private final int mPagesLength;

        public Adapter(FragmentManager fm, int pagesLength) {
            super(fm);
            mPagesLength = pagesLength;
        }

        @Override
        public Fragment getItem(int position) {
            return new PageFragment().setPageIndex(position);
        }

        @Override
        public int getCount() {
            return mPagesLength;
        }
    }

    public static class PageFragment extends Fragment {
        private int mPageIndex = -1;
        private int mShiftState;
        private boolean mHasButtons = true;

        public PageFragment setPageIndex(int mPageIndex) {
            this.mPageIndex = mPageIndex;
            return this;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if(savedInstanceState != null && mPageIndex == -1) {
                mPageIndex = savedInstanceState.getInt("pageIndex");
                mShiftState = savedInstanceState.getInt("shiftState");
                mHasButtons = savedInstanceState.getBoolean("hasButtons");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("pageIndex", mPageIndex);
            outState.putInt("shiftState", mShiftState);
            outState.putBoolean("hasButtons", mHasButtons);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            MainActivity activity = (MainActivity) getActivity();
            boolean isEastLocale = !Util.isWestLocale(this.getContext());
            PreferencesManager prefs = activity.getPrefs();

            ViewGroup view = (ViewGroup) KeyboardKitsXmlManager.createContentViewFromPage(this.getContext(),
                    activity.getKeyboardKits().mButtons, activity.getPreferredKeyboardKitVersion().mPages[mPageIndex],
                    inflater, isEastLocale, prefs.getAppTheme(), prefs.darkOrangeEquals(), activity);
            if(view.getChildCount() == 0) {
                mHasButtons = false;
                TextView tv = new TextView(getContext());
                tv.setText(R.string.message_emptyPage);
                tv.setTextColor(getResources().getColor(R.color.colorMessageEmptyPage));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                view.addView(tv, params);
            } else
                mHasButtons = true;
            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setShiftState(mShiftState, true);
        }

        private void setShiftState(int shiftState, boolean forceUpdate) {
            if(mShiftState == shiftState && !forceUpdate || !mHasButtons) return;

            boolean doInverse = ShiftButton.isInverseEnabled(shiftState);
            boolean changeText = forceUpdate || ShiftButton.isInverseEnabled(mShiftState) != doInverse;
            ArrayList<KeyboardKits.Button> buttons = ((MainActivity) getActivity()).getKeyboardKits().mButtons;
            ViewGroup root = (ViewGroup) getView();
            int lines = root.getChildCount();
            for (int i = 0; i < lines; i++) {
                ViewGroup line = (ViewGroup) root.getChildAt(i);
                int buttonsCount = line.getChildCount();
                for (int j = 0; j < buttonsCount; j++) {
                    View view = line.getChildAt(j);
                    if(view instanceof Button) {
                        Button btn = (Button) view;
                        KeyboardKits.Button btnInfo = buttons.get((int) btn.getTag());
                        if(!changeText || !btnInfo.mEnableCaseInverse) continue;
                        if (doInverse)
                            btn.setText(Util.inverseTextCase(btn.getText().toString()));
                        else
                            btn.setText(btnInfo.mName);
                    } else if(view instanceof ShiftButton)
                        ((ShiftButton) view).setState(shiftState);
                }
            }

            mShiftState = shiftState;
        }

        public int getCurrentShiftState() {
            return mShiftState;
        }
        public void setCurrentShiftState(int state) {
            setShiftState(state, false);
        }

        public int onPadButtonClicked() {
            int result = mShiftState;
            if(result == ShiftButton.STATE_ENABLED)
                setShiftState(ShiftButton.STATE_DISABLED, false);
            return result;
        }

        public void onShiftButtonClicked() {
            int newState = mShiftState + 1;
            if(newState > ShiftButton.STATE_CAPSLOCK)
                newState = ShiftButton.STATE_DISABLED;
            setShiftState(newState, false);
        }

        public void onSystemButtonClick(int property) {
            if(!mHasButtons) return;

            MainActivity activity = (MainActivity) getActivity();
            ViewGroup root = (ViewGroup) getView();
            int lines = root.getChildCount();
            boolean handled = false;
            for (int i = 0; i < lines; i++) {
                ViewGroup line = (ViewGroup) root.getChildAt(i);
                int buttonsCount = line.getChildCount();
                for (int j = 0; j < buttonsCount; j++) {
                    View view = line.getChildAt(j);
                    if(view instanceof SystemButton) {
                        SystemButton btn = (SystemButton) view;
                        if(btn.getProperty() == property && btn.onButtonClicked(activity, handled))
                            handled = true;
                    }
                }
            }
        }
    }
}
