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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import roottemplate.calculator.util.Util;

public class KeyboardKits {
    public enum ButtonType {
        EQUALS, DIGIT, BASE, SYMBOL, SHIFT, SYSTEM;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    public enum ButtonCategory {
        SYSTEM, DIGITS, OPERATORS, FUNCTIONS, LETTERS, MISCELLANEOUS, CUSTOM
    }
    public enum PageReturnType {
        NEVER, IF_DOUBLE_CLICK, ALWAYS;

        public static PageReturnType DEFAULT_VALUE = ALWAYS;

        @Override
        public String toString() {
            switch (this) {
                case NEVER: return "never";
                case IF_DOUBLE_CLICK: return "ifDouble";
                case ALWAYS: return "always";
            }
            return null;
        }

        public static PageReturnType getValueOf(String str) {
            if(str == null) return null;
            return str.equalsIgnoreCase("ifDouble") ? PageReturnType.IF_DOUBLE_CLICK :
                    PageReturnType.valueOf(str.toUpperCase());
        }
    }

    public static class Button {
        public String mName;
        public String mText;
        public ButtonType mType;
        public int mLocaleEastId;
        public boolean mEnableCaseInverse;
        public ButtonCategory mCategory;
        public PageReturnType mOverriddenPageReturn;

        public Button(String name, String text, String type, int localeEast, boolean inverseOnLongClick,
                      ButtonCategory category, String pageReturn) {
            this(name, text, ButtonType.valueOf(type.toUpperCase()), localeEast, inverseOnLongClick,
                    category, PageReturnType.getValueOf(pageReturn));
        }
        public Button(String text, ButtonType type, boolean enableCaseInverse, ButtonCategory category) {
            this(text, text, type, -1, enableCaseInverse, category);
        }
        public Button(String name, String text, ButtonType type, int localeEastId, boolean enableCaseInverse,
                      ButtonCategory category) {
            this(name, text, type, localeEastId, enableCaseInverse, category, null);
        }
        public Button(String name, String text, ButtonType type, int localeEastId, boolean enableCaseInverse,
                      ButtonCategory category, PageReturnType pageReturn) {
            mName = name;
            mText = text;
            mType = type;
            mLocaleEastId = localeEastId;
            mEnableCaseInverse = enableCaseInverse;
            mCategory = category;
            mOverriddenPageReturn = pageReturn;
        }

        public void dumpToXml(XmlSerializer f, int btnId) throws IOException {
            f.startTag("", "Button")
                    .attribute("", "id", Integer.toString(btnId))
                    .attribute("", "name", mName)
                    .attribute("", "text", mText)
                    .attribute("", "type", mType.toString())
                    .attribute("", "enableCaseInverse", Boolean.toString(mEnableCaseInverse));
                    // Custom buttons cannot have localeEastId
            if(mOverriddenPageReturn != null)
                f.attribute("", "overriddenPageReturn", mOverriddenPageReturn.toString());
            f.endTag("", "Button");
        }

        @Override
        public String toString() {
            return "Button {name: " + mName + ", text: " + mText + ", type: " + mType.toString() +
                    ", localeEast: " + mLocaleEastId + ", enableCaseInverse: " + mEnableCaseInverse
                    + ", category: " + mCategory + ", pageReturn: " + mOverriddenPageReturn + "}";
        }
    }

    public static class Page {
        public PageReturnType mMoveToMain;
        /** true if layout has orientation="vertical", false if orientation="horizontal" **/
        public boolean mIsVerticalOrient;
        public int[][] mButtons;

        public Page(String moveToMain, boolean isVerticalOrient, int[][] buttons) {
            this(PageReturnType.getValueOf(moveToMain), isVerticalOrient, buttons);
        }
        public Page(PageReturnType moveToMain, boolean isVerticalOrient, int[][] buttons) {
            mMoveToMain = moveToMain;
            mIsVerticalOrient = isVerticalOrient;
            mButtons = buttons;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Page {moveToMain: " + (mMoveToMain == null ? null : mMoveToMain.toString())
                    + ", verticalOrientation: " + mIsVerticalOrient + ", buttons: [");
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

        public void dumpToXml(XmlSerializer f, boolean isMainPage) throws IOException {
            f.startTag("", "Page")
                    .attribute("", "main", Boolean.toString(isMainPage))
                    .attribute("", "layoutOrientation", mIsVerticalOrient ? "vertical" : "horizontal");

            if(mMoveToMain != null)
                f.attribute("", "moveToMain", mMoveToMain.toString());
            for(int[] row : mButtons) {
                f.startTag("", "PageRow");
                for(int btnId : row) {
                    if(btnId >= DEFAULT_BUTTONS_COUNT) btnId = DEFAULT_BUTTONS_COUNT - btnId - 1;
                    f.startTag("", "Button")
                            .attribute("", "id", Integer.toString(btnId))
                            .endTag("", "Button");
                }
                f.endTag("", "PageRow");
            }
            f.endTag("", "Page");
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

        public void dumpToXml(XmlSerializer f) throws IOException {
            f.startTag("", "Version")
                    .attribute("", "orientation", mIsLandscapeOrient ? "landscape" : "portrait");
            int i = 0;
            for(Page page : mPages) {
                page.dumpToXml(f, i == mMainPageIndex);
                i++;
            }
            f.endTag("", "Version");
        }
    }

    public static class Kit {
        public String mName;
        public String mShortName;
        public boolean mActionBarAccess;
        public KitVersion[] mKitVersions;
        public final boolean mIsSystem;
        public int mNumberOutputType;

        public Kit(String name, String shortName, boolean actionBarAccess, boolean isSystem,
                   KitVersion[] kitVersions, int numberOutputType) {
            mName = name;
            mShortName = shortName;
            mActionBarAccess = actionBarAccess;
            mKitVersions = kitVersions;
            mIsSystem = isSystem;
            mNumberOutputType = numberOutputType;

            for(KitVersion kv : kitVersions)
                kv.mParent = this;
        }

        public String getFullName() {
            String result = mName;
            if(mShortName != null && !mShortName.isEmpty())
                result += " (" + mShortName + ")";
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Kit {");
            sb
                    .append("name: ").append(mName).append(", ")
                    .append("shortName: ").append(mShortName).append(", ")
                    .append("actionBarAccess: ").append(mActionBarAccess).append(", ")
                    .append("isSystem: ").append(mIsSystem).append(", ")
                    .append("numberOutputType: ").append(mNumberOutputType).append(", ")
                    .append("kitVersions: [");
            for (int i = 0; i < mKitVersions.length; i++) {
                if (i != 0)
                    sb.append(",\n");
                sb.append(mKitVersions[i].toString());
            }
            return sb.append("]}").toString();
        }

        public void dumpToXml(XmlSerializer f) throws IOException {
            f.startTag("", "Kit")
                    .attribute("", "isSystem", Boolean.toString(mIsSystem))
                    .attribute("", "name", mName)
                    .attribute("", "actionBarAccess", Boolean.toString(mActionBarAccess));
            if(mShortName != null) f.attribute("", "shortName", mShortName);
            if(mNumberOutputType != -1)
                f.attribute("", "numberOutputType", Integer.toString(mNumberOutputType));

            if(!mIsSystem)
                for(KitVersion kv : mKitVersions)
                    kv.dumpToXml(f);
            f.endTag("", "Kit");
        }
    }


    public ArrayList<Button> mButtons;
    public ArrayList<Kit> mKits;

    public KeyboardKits(ArrayList<Button> buttons, ArrayList<Kit> kits) {
        mButtons = buttons;
        mKits = kits;
    }

    @Override
    public String toString() {
        boolean first = true;
        StringBuilder sb = new StringBuilder("ButtonKits {buttons: [");
        for(Button btn : mButtons) {
            if(first)
                first = false;
            else
                sb.append(",\n");
            sb.append(btn.toString());
        }
        sb.append("],\n\n\nkits: [");
        first = true;
        for(Kit kit : mKits) {
            if(first)
                first = false;
            else
                sb.append(",\n\n");
            sb.append(kit.toString());
        }
        return sb.append("]}").toString();
    }

    public void dumpToXml(XmlSerializer f) throws IOException {
        f.startTag("", "KeyboardKits");
        f.startTag("", "CustomButtons");
        ListIterator<Button> it = mButtons.listIterator(DEFAULT_BUTTONS_COUNT);
        for(int i = -1; it.hasNext(); i--) {
            Button btn = it.next();
            btn.dumpToXml(f, i);
        }
        f.endTag("", "CustomButtons");
        f.startTag("", "Kits");
        for(Kit kit : mKits)
            kit.dumpToXml(f);
        f.endTag("", "Kits");
        f.endTag("", "KeyboardKits");
    }



    public static KitVersion[] cloneKitVersionsFrom(Kit kit) {
        KitVersion[] res = new KitVersion[kit.mKitVersions.length];
        int i = 0;
        for(KitVersion kv : kit.mKitVersions) {
            Page[] pages = new Page[kv.mPages.length];
            int j = 0;
            for(Page p : kv.mPages) {
                int[][] buttons = Util.cloneIntMatrix(p.mButtons);
                pages[j] = new Page(p.mMoveToMain, p.mIsVerticalOrient, buttons);
                j++;
            }

            KitVersion newKv = new KitVersion(pages, kv.mIsLandscapeOrient, kv.mMainPageIndex);
            res[i] = newKv;
            i++;
        }
        return res;
    }


    public static final int DEFAULT_BUTTONS_COUNT = 66;
    public static final String DEFAULT_KIT_NAME = "default";
    public static Button[] generateDefaultButtonArray() {
        Button[] r = new Button[DEFAULT_BUTTONS_COUNT];
        r[0] = new Button("=", ButtonType.EQUALS, false, ButtonCategory.SYSTEM);
        r[1] = new Button("0", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[2] = new Button("1", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[3] = new Button("2", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[4] = new Button("3", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[5] = new Button("4", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[6] = new Button("5", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[7] = new Button("6", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[8] = new Button("7", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[9] = new Button("8", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[10] = new Button("9", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[11] = new Button(",", ButtonType.SYMBOL, false, ButtonCategory.MISCELLANEOUS);
        r[12] = new Button("e", ButtonType.SYMBOL, true, ButtonCategory.LETTERS);
        r[12 + 1] = new Button("\u03c0", ButtonType.SYMBOL, true, ButtonCategory.LETTERS); // PI
        r[14] = new Button("(", ButtonType.SYMBOL, true, ButtonCategory.MISCELLANEOUS);
        r[15] = new Button(")", ButtonType.SYMBOL, true, ButtonCategory.MISCELLANEOUS);
        r[16] = new Button("|x|", "abs(", ButtonType.SYMBOL, -1, false, ButtonCategory.FUNCTIONS);
        r[17] = new Button("+", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS);
        r[18] = new Button("\u2212", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS); // minus
        r[19] = new Button("\u00d7", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS); // multiply
        r[20] = new Button("\u00f7", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS); // divide
        r[21] = new Button("!", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS);
        r[22] = new Button("^", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS);
        r[23] = new Button("\u221a", ButtonType.SYMBOL, false, ButtonCategory.OPERATORS); // sqrt
        r[24] = new Button(".", ButtonType.DIGIT, false, ButtonCategory.DIGITS);
        r[25] = new Button("\u221e", ButtonType.SYMBOL, false, ButtonCategory.MISCELLANEOUS); // infinity
        r[26] = new Button("NaN", ButtonType.SYMBOL, false, ButtonCategory.MISCELLANEOUS);
        r[27] = new Button("E", ButtonType.SYMBOL, true, ButtonCategory.MISCELLANEOUS);
        r[28] = new Button("%", ButtonType.SYMBOL, false, ButtonCategory.MISCELLANEOUS);
        r[29] = new Button("=", ButtonType.SYMBOL, false, ButtonCategory.SYSTEM);
        
        r[30] = new Button("sin", "sin(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[31] = new Button("cos", "cos(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[32] = new Button("tan", "tan(", ButtonType.BASE, 36, false, ButtonCategory.FUNCTIONS);
        r[33] = new Button("asin", "asin(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[34] = new Button("acos", "acos(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[35] = new Button("atan", "atan(", ButtonType.BASE, 37, false, ButtonCategory.FUNCTIONS);
        r[36] = new Button("tg", "tg(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[37] = new Button("atg", "atg(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[38] = new Button("log", "log(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[39] = new Button("lg", "lg(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        r[40] = new Button("ln", "ln(", ButtonType.BASE, -1, false, ButtonCategory.FUNCTIONS);
        
        r[41] = new Button("", ButtonType.SHIFT, false, ButtonCategory.SYSTEM);
        r[42] = new Button("a", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[43] = new Button("b", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[44] = new Button("c", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[45] = new Button("f", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[46] = new Button("g", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[47] = new Button("h", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[48] = new Button("k", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[49] = new Button("m", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[50] = new Button("n", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[51] = new Button("r", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[52] = new Button("s", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[53] = new Button("t", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[54] = new Button("x", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[55] = new Button("y", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[56] = new Button("z", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[57] = new Button("α", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[58] = new Button("β", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[59] = new Button("μ", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[60] = new Button("ν", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[61] = new Button("φ", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[62] = new Button("ρ", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[63] = new Button("i", ButtonType.BASE, true, ButtonCategory.LETTERS);
        r[64] = new Button("q", ButtonType.BASE, true, ButtonCategory.LETTERS);
        
        r[65] = new Button("amu", ButtonType.SYSTEM, false, ButtonCategory.SYSTEM);
        // 66 elements; if you change this value, don't forget to change it in the
        // DEFAULT_BUTTONS_COUNT field
        return r;
    }

    public static KitVersion[] generateDefaultKitVersions() {
        KitVersion portrait = new KitVersion(new Page[] {
                        new Page(PageReturnType.IF_DOUBLE_CLICK, true, new int[][] {
                                new int[] {57, 58, 59, 60, 61},
                                new int[] {42, 43, 44, 45, 46},
                                new int[] {48, 63, 49, 50, 51},
                                new int[] {52, 53, 54, 55, 56},
                                new int[] {41, 14, 11, 15, 29},
                        }),
                        new Page(PageReturnType.NEVER, true, new int[][] {
                                new int[] {11, 12, 13, 14, 15},
                                new int[] {27, 8, 9, 10, 20},
                                new int[] {21, 5, 6, 7, 19},
                                new int[] {22, 2, 3, 4, 18},
                                new int[] {23, 1, 24, 0, 17},
                        }),
                        new Page(PageReturnType.ALWAYS, false, new int[][] {
                                new int[] {30, 31, 32},
                                new int[] {33, 34, 35},
                                new int[] {38, 39, 40},
                                new int[] {16, 25, 28, 26, 65},
                        })
        }, false, 1);
        KitVersion landscape = new KitVersion(new Page[] {
                        new Page(PageReturnType.IF_DOUBLE_CLICK, false, new int[][] {
                                new int[] {57, 58, 59, 60},
                                new int[] {62, 61, 63, 64},
                                new int[] {42, 43, 44, 45},
                                new int[] {46, 47, 48, 49},
                                new int[] {50, 51, 52, 53},
                                new int[] {54, 55, 56, 41},
                                new int[] {14, 11, 15, 29},
                        }),
                        new Page(PageReturnType.NEVER, true, new int[][] {
                                new int[] {14, 15, 8, 9, 10, 20},
                                new int[] {21, 11, 5, 6, 7, 19},
                                new int[] {22, 13, 2, 3, 4, 18},
                                new int[] {23, 27, 1, 24, 0, 17},
                        }),
                        new Page(PageReturnType.ALWAYS, false, new int[][] {
                                new int[] {30, 31, 32},
                                new int[] {33, 34, 35},
                                new int[] {38, 39, 40},
                                new int[] {12, 28, 25},
                                new int[] {16, 26, 65},
                        })
        }, true, 1);
        return new KitVersion[] {portrait, landscape};
    }
}
