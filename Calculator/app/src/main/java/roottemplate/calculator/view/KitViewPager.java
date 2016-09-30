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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import roottemplate.calculator.MainActivity;
import roottemplate.calculator.pads.ButtonKitsXmlFile;
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

        public PageFragment setPageIndex(int mPageIndex) {
            this.mPageIndex = mPageIndex;
            return this;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("pageIndex", mPageIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if(savedInstanceState != null)
                mPageIndex = savedInstanceState.getInt("pageIndex");

            MainActivity activity = (MainActivity) getActivity();
            boolean isEastLocale = !Util.isWestLocale(this.getContext());
            return ButtonKitsXmlFile.createContentViewFromPage(this.getContext(),
                    activity.getButtonKits().mButtons, activity.getPreferredButtonKitVersion().mPages[mPageIndex],
                    inflater, isEastLocale, activity);
        }
    }
}
