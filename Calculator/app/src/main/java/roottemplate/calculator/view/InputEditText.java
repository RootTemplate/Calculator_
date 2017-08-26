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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import roottemplate.calculator.R;
import roottemplate.calculator.evaluator.util.ExpressionFormatUpdater;
import roottemplate.calculator.util.ExpressionFormatter;
import roottemplate.calculator.util.Util;

public class InputEditText extends android.support.v7.widget.AppCompatEditText {
    public static final char DIGIT_DELIMITER = ' ';

    public static String intToDigitSeparator(int i) {
        switch (i) {
            case 2: return ",";
            case 3: return "'";
            default: return "";
        }
    }

    private float spToPx(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }



    private final ExpressionFormatUpdater mUpdater = new ExpressionFormatUpdater(DIGIT_DELIMITER, '.', 0);
    private byte mExprDigitGroupingMask;
    private boolean mExprDoGroupDigits;
    private String mDigitSeparatorLeft;
    private String mDigitSeparatorFract;
    private boolean mHighlightE;

    private TextType mTextType;
    private String mNumberReplacement;
    private Rect r = new Rect();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    private MenuHandler mHandler = new MenuHandler();
    private InputCallback mInputCallback;
    private int mMaxDigitsToFit = -1;
    private long mTimeSinceDown;

    public InputEditText(Context context) {
        super(context);
        init();
    }

    public InputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setTextType(ss.mTextType);
        mNumberReplacement = ss.mMessageReplacement;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), mTextType, mNumberReplacement);
    }

    private void init() {
        setCustomSelectionActionModeCallback(new NoTextSelectionMode());
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mUpdater.updateContainer = new ExpressionFormatUpdater.Update();
        setTextType(TextType.INPUT);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(spToPx(45));
        paint.setTextAlign(Paint.Align.LEFT);
    }

    public void calcMaxDigits() {
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        mMaxDigitsToFit = (int) Math.floor((getWidth() - 5) / paint.measureText("8"));
    }
    public int getMaxDigitsToFit() {
        return mMaxDigitsToFit;
    }


    public void setInputCallback(InputCallback callback) {
        mInputCallback = callback;
    }

    private void updateDigitGrouping(int updStart, int updEnd, boolean force) {
        if(!force && !mUpdater.doGroup()) return;

        ExpressionFormatUpdater.Update u =
                mUpdater.updateDigitGroupingInitial(getText(), getSelectionStart(), updStart, updEnd);
        getEditableText().replace(u.begin(), u.end(), u.replaceTo());
        setSelection(u.cursor());
    }

    public void initDigitFormatting(boolean doGroup, int left, int fractional, boolean highlightE) {
        int mask = 0;
        if(left > 0) mask |= ExpressionFormatter.DIGIT_GROUPING_LEFT;
        if(fractional > 0) mask |= ExpressionFormatter.DIGIT_GROUPING_FRACTIONAL;

        mExprDigitGroupingMask = (byte) mask;
        mExprDoGroupDigits = doGroup;
        mDigitSeparatorLeft = intToDigitSeparator(left);
        mDigitSeparatorFract = intToDigitSeparator(fractional);
        mHighlightE = highlightE;

        TextType type = mTextType;
        mTextType = null;
        setTextType(type);
    }

    private void updateEHighlights(int startChangedIndex, int endChangedIndex) {
        Editable text = super.getText();
        int startIndex = startChangedIndex > 0 ? startChangedIndex - 1 : 0;
        int endIndex = endChangedIndex < text.length() - 1 ? endChangedIndex + 1 : endChangedIndex;
        if(mHighlightE && mTextType != TextType.RESULT_MESSAGE) {
            int eTextSize = getContext().getResources().getDimensionPixelSize(R.dimen.inputTextEHighlight);

            for(int i = startIndex; i < endIndex; i++) {
                if(text.charAt(i) != 'E') continue;
                boolean eShouldBeHighlighted = false;
                if(i > 0 && i < text.length() - 1) {
                    char prev = text.charAt(i - 1);
                    char next = text.charAt(i + 1);
                    if((Character.isDigit(prev) || prev == '.') &&
                            (Character.isDigit(next) || next == '+' || next == '-' || next == '\u2212') ) {
                        text.setSpan(new AbsoluteSizeSpan(eTextSize), i, i + 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        eShouldBeHighlighted = true;
                    }
                }
                if(!eShouldBeHighlighted) {
                    for(AbsoluteSizeSpan span : text.getSpans(i, i + 1, AbsoluteSizeSpan.class))
                        text.removeSpan(span);
                }
            }
        }
    }

    /**
     * Appends <code>str</code> to expression in this text field
     * @param str The string to append
     * @param clearIfResult If current TextType is RESULT(_NUMBER or _MESSAGE) then expression is
     *                      previously cleared
     */
    public void appendText(String str, boolean clearIfResult) {
        if(clearIfResult && mTextType != TextType.INPUT)
            clearText();
        else
            setTextType(TextType.INPUT);

        if(str.isEmpty()) return;
        Editable text = getEditableText();
        int cursor = getSelectionStart();

        text.insert(cursor, str);
        cursor += str.length();
        setSelection(cursor);

        int startIndex = cursor - str.length();
        updateEHighlights(startIndex, cursor);
        updateDigitGrouping(startIndex, cursor - 1, false);
    }
    public void setText(String str) {
        clearText();
        appendText(str, true);
    }
    public void setTextNoTextTypeChange(String str) {
        super.setText(str);
        setCursor(str.length());
        updateEHighlights(0, str.length());
    }
    public String getExprText() {
        CharSequence text = getText();
        StringBuilder sb = new StringBuilder(text.length());
        int grouping = mUpdater.getGrouping();
        char separator = mUpdater.digitDelimiter;

        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(grouping == 0 || c != separator)
                sb.append(c);
        }

        return sb.toString();
    }
    public void clearText() {
        mNumberReplacement = null; // Make sure that no text will appear after setting TextType.INPUT
        super.setText("");
        setTextType(TextType.INPUT);
    }
    public void delSymbol() {
        mNumberReplacement = null;
        setTextType(TextType.INPUT);

        int cursor = getSelectionStart();
        if(cursor <= 0) return;

        getEditableText().delete(cursor - 1, cursor);
        setSelection(--cursor);
        updateEHighlights(cursor, cursor);
        updateDigitGrouping(cursor, cursor - 1, false);
    }
    public void setCursor(int cursor) {
        setSelection(cursor);
    }

    public void setNumberReplacement(String numberReplacement) {
        mNumberReplacement = numberReplacement;
    }

    public void setTextType(TextType type) {
        if(type == mTextType) return;

        int mask = mExprDoGroupDigits ? mExprDigitGroupingMask : ExpressionFormatter.DIGIT_GROUPING_NONE;
        mUpdater.setGrouping(mask);
        updateDigitGrouping(0, getText().length() - 1, true);

        if(type == TextType.INPUT) {
            setTextColor(getResources().getColor(R.color.colorInputText));
            paint.setColor(getResources().getColor(R.color.colorInputSeparator));
            setCursorVisible(true);

            if(mTextType == TextType.RESULT_NUMBER && mNumberReplacement != null) {
                Editable text = getText();
                text.replace(0, text.length(), mNumberReplacement);
            } else if(mTextType == TextType.RESULT_MESSAGE)
            	super.setText("");
        } else {
            setTextColor(getResources().getColor(R.color.colorEquals));
            paint.setColor(getResources().getColor(R.color.colorInputSeparatorResult));
            setCursorVisible(false);
        }

        mTextType = type;
        updateEHighlights(0, getText().length());
    }
    public TextType getTextType() {
        return mTextType;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        new MenuInflater(getContext()).inflate(R.menu.menu_input_context, menu);

        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setOnMenuItemClickListener(mHandler);
            switch (item.getItemId()) {
                case R.id.menuItem_cut:
                case R.id.menuItem_copy:
                    if(getText().toString().isEmpty())
                        item.setVisible(false);
                    break;
                case R.id.menuItem_paste:
                    if(Util.getPrimaryClip(getContext()).isEmpty())
                        item.setVisible(false);
                    break;
                case R.id.menuItem_asFraction:
                    item.setVisible(mTextType == TextType.RESULT_NUMBER && mInputCallback != null &&
                            mInputCallback.canShowResultAsFraction(this));
                    break;
            }

        }
    }



    /*private boolean isFromMakeBlink() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(StackTraceElement elem : stack) {
            if(     elem.getClassName().endsWith("$Blink") &&
                    elem.getClassName().startsWith("android.widget.") &&
                    elem.getMethodName().equals("run"))
                return true;
        }
        return false;
    }

    @Override
    public void invalidate() {
        if(isFromMakeBlink()) return;
        super.invalidate();
    }

    @Override
    public void invalidate(@NonNull Rect dirty) {
        if(isFromMakeBlink()) return;
        super.invalidate(dirty);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        if(isFromMakeBlink()) return;
        super.invalidate(l, t, r, b);
    }*/


    @Override
    protected void onDraw(Canvas canvas) {
        getLineBounds(0, r);

        String text = super.getText().toString();
        float xOffset = 0, yOffset = -spToPx(29);
        boolean point = false;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c == DIGIT_DELIMITER) {
                float textWidth = getPaint().measureText(text, i, text.length());
                canvas.drawText(!point ? mDigitSeparatorLeft : mDigitSeparatorFract,
                        r.right - textWidth + xOffset, r.top + getHeight() + yOffset, paint);
            } else if(c == '.') {
                point = true;
            } else if(point && !Character.isDigit(c)) {
                point = false;
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        mTimeSinceDown = 0;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(!hasWindowFocus())
            return true;

        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mTimeSinceDown = System.currentTimeMillis();
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // Hack to prevent keyboard and insertion handle from showing.
            cancelLongPress();

            if(System.currentTimeMillis() <= mTimeSinceDown + 500) {
                Layout layout = getLayout();
                float x = event.getX() + getScrollX();
                float y = event.getY() + getScrollY();
                int line = layout.getLineForVertical((int) y);
                int offset = layout.getOffsetForHorizontal(line, x);

                if (offset < 0) offset = 0;
                if (offset >= 1 && getText().charAt(offset - 1) == DIGIT_DELIMITER)
                    offset--;
                setSelection(offset);

                setTextType(TextType.INPUT);
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performLongClick() {
        showContextMenu();
        return true;
    }



    public interface InputCallback {
        boolean canShowResultAsFraction(InputEditText v);
        void onShowResultAsFraction(InputEditText v);
    }

    private class MenuHandler implements MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()) {
                case R.id.menuItem_cut:
                    copy();
                    clearText();
                    return true;
                case R.id.menuItem_copy:
                    copy();
                    return true;
                case R.id.menuItem_paste:
                    appendText(Util.getPrimaryClip(getContext()), true);
                    return true;
                case R.id.menuItem_asFraction:
                    if(mInputCallback != null)
                        mInputCallback.onShowResultAsFraction(InputEditText.this);
                    break;
            }
            return false;
        }

        public void copy() {
            Util.setPrimaryClip(getContext(), getText().toString());
        }
    }

    private class NoTextSelectionMode implements ActionMode.Callback {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mHandler.copy();
            // Prevents the selection action mode on double tap.
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {}
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }

    public enum TextType {
        INPUT, RESULT_NUMBER, RESULT_MESSAGE
    }

    public static class SavedState extends BaseSavedState {
        private TextType mTextType;
        private String mMessageReplacement;

        private SavedState(Parcelable superState, TextType textType, String messageReplacement) {
            super(superState);
            mTextType = textType;
            mMessageReplacement = messageReplacement;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mTextType.ordinal());
            out.writeString(mMessageReplacement);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            mTextType = TextType.values()[in.readInt()];
            mMessageReplacement = in.readString();
        }
    }
}
