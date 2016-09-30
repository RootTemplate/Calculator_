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

package roottemplate.calculator.util;

import android.text.Editable;

import roottemplate.calculator.evaluator.util.ExpressionFormatUpdater;

public class ExpressionFormatter {
    public static final int DIGIT_GROUPING_NONE = ExpressionFormatUpdater.DIGIT_GROUPING_NONE;
    public static final int DIGIT_GROUPING_LEFT = ExpressionFormatUpdater.DIGIT_GROUPING_LEFT;
    public static final int DIGIT_GROUPING_FRACTIONAL = ExpressionFormatUpdater.DIGIT_GROUPING_FRACTIONAL;

    private Editable mText;
    private final ExpressionFormatUpdater mUpdater;
    private int cursor = 0;

    public ExpressionFormatter(Editable text, char digitDelimiter, char point, int grouping) {
        if(text == null) throw new NullPointerException();

        mText = text;
        mUpdater = new ExpressionFormatUpdater(digitDelimiter, point, grouping);
        mUpdater.updateContainer = new ExpressionFormatUpdater.Update();
    }

    private void updateDigitGrouping(int updStart, int updEnd) {
        if(!mUpdater.doGroup()) return;

        ExpressionFormatUpdater.Update u =
                mUpdater.updateDigitGroupingInitial(mText.toString(), cursor, updStart, updEnd);
        mText.replace(u.begin(), u.end(), u.replaceTo());
        cursor = u.cursor();
    }

    public void append(String s) {
        if(s.isEmpty()) return;

        mText.insert(cursor, s);
        cursor += s.length();
        updateDigitGrouping(cursor - s.length(), cursor - 1);
    }

    public void backspace() {
        if(cursor == 0) return;

        cursor--;
        mText.delete(cursor, cursor + 1);
        updateDigitGrouping(cursor, cursor - 1);
    }

    public void clear() {
        mText.delete(0, mText.length());
        cursor = 0;
    }

    public void set(String s) {
        clear();
        append(s);
    }

    public void setCursor(int cursor) {
        if(cursor < 0)
            cursor = 0;
        else if(cursor > mText.length())
            cursor = mText.length();

        if(mUpdater.doGroup() && cursor > 0 && mText.charAt(cursor - 1) == mUpdater.digitDelimiter)
            cursor--;

        this.cursor = cursor;
    }
    public int getCursor() {
        return cursor;
    }

    public void setGrouping(int grouping) {
        if(mUpdater.getGrouping() == grouping) return;
        mUpdater.setGrouping(grouping);
        updateDigitGrouping(0, mText.length() - 1);
    }
    public int getGrouping() {
        return mUpdater.getGrouping();
    }

    public Editable getText() {
        return mText;
    }
    public void setText(Editable newText) {
        if(newText == null) throw new NullPointerException();
        mText = newText;
    }

    @Override
    public String toString() {
        return mText.toString();
    }

    public String toExpr() {
        StringBuilder sb = new StringBuilder(mText.length());
        int grouping = mUpdater.getGrouping();
        char separator = mUpdater.digitDelimiter;

        for(int i = 0; i < mText.length(); i++) {
            char c = mText.charAt(i);
            if(grouping == 0 || c != separator)
                sb.append(c);
        }

        return sb.toString();
    }
}
