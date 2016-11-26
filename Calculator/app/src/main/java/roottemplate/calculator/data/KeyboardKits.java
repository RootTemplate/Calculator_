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

package roottemplate.calculator.data;

public class KeyboardKits {
    public enum ButtonType {
        EQUALS, DIGIT, BASE, SYMBOL, SHIFT
    }
    public enum PageReturnType {
        NEVER, IF_DOUBLE_CLICK, ALWAYS
    }

    public static class Button {
        public String mName;
        public String mText;
        public ButtonType mType;
        public int mLocaleEastId;
        public boolean mEnableCaseInverse;

        public Button(String name, String text, String type, String localeEast, boolean inverseOnLongClick) {
            this(name, text, ButtonType.valueOf(type.toUpperCase()),
                    localeEast == null ? -1 : Integer.parseInt(localeEast), inverseOnLongClick);
        }
        public Button(String name, String text, ButtonType type, int localeEastId, boolean enableCaseInverse) {
            mName = name;
            mText = text;
            mType = type;
            mLocaleEastId = localeEastId;
            mEnableCaseInverse = enableCaseInverse;
        }

        @Override
        public String toString() {
            return "Button {name: " + mName + ", text: " + mText + ", type: " + mType.toString() +
                    ", localeEast: " + mLocaleEastId + ", enableCaseInverse: " + mEnableCaseInverse
                    + "}";
        }
    }

    public static class Page {
        public PageReturnType mMoveToMain;
        public boolean mIsLayoutLandscape;
        public int[][] mButtons;

        public Page(String moveToMain, boolean isLayoutLandscape, int[][] buttons) {
            this(moveToMain != null ? (moveToMain.equalsIgnoreCase("ifDouble") ?
                            PageReturnType.IF_DOUBLE_CLICK : PageReturnType.valueOf(moveToMain.toUpperCase()))
                    : null,
                    isLayoutLandscape, buttons);
        }
        public Page(PageReturnType moveToMain, boolean isLayoutLandscape, int[][] buttons) {
            mMoveToMain = moveToMain;
            mIsLayoutLandscape = isLayoutLandscape;
            mButtons = buttons;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Page {moveToMain: " + (mMoveToMain == null ? null : mMoveToMain.toString())
                    + ", layoutLandscape: " + mIsLayoutLandscape + ", buttons: [");
            for(int i = 0; i < mButtons.length; i++) {
                if(i != 0)
                    sb.append("], [");
                for(int j = 0; j < mButtons[i].length; j++) {
                    if(j != 0)
                        sb.append(", ");
                    sb.append(mButtons[i][j]);
                }
            }
            return sb.append("]}").toString();
        }
    }

    public static class KitVersion {
        public Page[] mPages;
        public boolean mIsLandscapeOrient;
        public int mMainPageIndex;
        public Kit mParent;

        public KitVersion(Page[] pages, boolean orientation, int mainPageIndex) {
            mPages = pages;
            mIsLandscapeOrient = orientation;
            mMainPageIndex = mainPageIndex;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("KitVersion {isLandscapeOrient: ").append(mIsLandscapeOrient)
                    .append(", mainPageIndex: ").append(mMainPageIndex)
                    .append(", pages: [");

            for (int i = 0; i < mPages.length; i++) {
                if (i != 0)
                    sb.append(",\n");
                sb.append(mPages[i].toString());
            }

            return sb.append("]}").toString();
        }
    }

    public static class Kit {
        public String mName;
        public String mShortName;
        public boolean mActionBarAccess;
        public KitVersion[] mKitVersions;

        public Kit(String name, String shortName, boolean actionBarAccess, KitVersion[] kitVersions) {
            mName = name;
            mShortName = shortName;
            mActionBarAccess = actionBarAccess;
            mKitVersions = kitVersions;

            for(KitVersion kv : kitVersions)
                kv.mParent = this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Kit {");
            sb
                    .append("name: ").append(mName).append(", ")
                    .append("shortName: ").append(mShortName).append(", ")
                    .append("actionBarAccess: ").append(mActionBarAccess).append(", ")
                    .append("kitVersions: [");
            for (int i = 0; i < mKitVersions.length; i++) {
                if (i != 0)
                    sb.append(",\n");
                sb.append(mKitVersions[i].toString());
            }
            return sb.append("]}").toString();
        }
    }


    public Button[] mButtons;
    public Kit[] mKits;
    public boolean mIsDefault;

    public KeyboardKits(Button[] buttons, Kit[] kits, boolean isDefault) {
        mButtons = buttons;
        mKits = kits;
        mIsDefault = isDefault;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ButtonKits {");
        sb.append("isDefault: ").append(mIsDefault).append(", buttons: [");
        for(int i = 0; i < mButtons.length; i++) {
            if(i != 0)
                sb.append(",\n");
            sb.append(mButtons[i].toString());
        }
        sb.append("],\n\n\nkits: [");
        for(int i = 0; i < mKits.length; i++) {
            if(i != 0)
                sb.append(",\n\n");
            sb.append(mKits[i].toString());
        }
        return sb.append("]}").toString();
    }
}
