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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import roottemplate.calculator.MainActivity;
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
        private int mPageIndex;
        private int mShiftState;

        public PageFragment setPageIndex(int mPageIndex) {
            this.mPageIndex = mPageIndex;
            return this;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if(savedInstanceState != null) {
                mPageIndex = savedInstanceState.getInt("pageIndex");
                mShiftState = savedInstanceState.getInt("shiftState");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("pageIndex", mPageIndex);
            outState.putInt("shiftState", mShiftState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            MainActivity activity = (MainActivity) getActivity();
            boolean isEastLocale = !Util.isWestLocale(this.getContext());
            return KeyboardKitsXmlManager.createContentViewFromPage(this.getContext(),
                    activity.getKeyboardKits().mButtons, activity.getPreferredKeyboardKitVersion().mPages[mPageIndex],
                    inflater, isEastLocale, activity);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setShiftState(mShiftState, true);
        }

        private void setShiftState(int shiftState, boolean forceUpdate) {
            if(mShiftState == shiftState && !forceUpdate) return;

            boolean doInverse = ShiftButton.isInverseEnabled(shiftState);
            boolean changeText = forceUpdate || ShiftButton.isInverseEnabled(mShiftState) != doInverse;
            KeyboardKits.Button[] buttons = ((MainActivity) getActivity()).getKeyboardKits().mButtons;
            ViewGroup root = (ViewGroup) getView();
            int lines = root.getChildCount();
            for (int i = 0; i < lines; i++) {
                ViewGroup line = (ViewGroup) root.getChildAt(i);
                int buttonsCount = line.getChildCount();
                for (int j = 0; j < buttonsCount; j++) {
                    View view = line.getChildAt(j);
                    if(view instanceof Button) {
                        if(!changeText) continue;
                        Button btn = (Button) view;
                        if (doInverse)
                            btn.setText(Util.inverseTextCase(btn.getText().toString()));
                        else
                            btn.setText(buttons[(int) btn.getTag()].mName);
                    } else if(view instanceof ShiftButton)
                        ((ShiftButton) view).setState(shiftState);
                }
            }

            mShiftState = shiftState;
        }

        public int getCurrentShiftState() {
            return mShiftState;
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
    }
}
