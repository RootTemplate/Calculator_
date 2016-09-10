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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;

import roottemplate.calculator.R;
import roottemplate.calculator.util.Util;

public class InputEditText extends EditText {
    /*public static final char DIGIT_DELIMITER = ' ';

    private static String addDigitDelimiters(String str) {
        return str;
        str = str.replace(new String(new char[] {DIGIT_DELIMITER}), "");
        StringBuilder sb = new StringBuilder(str.length());
        byte number = -1; // -1 - not found; 0 - found but searching for point; 1 - processing number
        int toPoint = 0;
        int numberStart = -1;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(number == 0 && (!Character.isDigit(c) || i == str.length() - 1)) {
                toPoint = i - numberStart;
                i = numberStart - 1; // And after that i++ will be invoked
                number = 1;
            } else if(!Character.isDigit(c)) {
                sb.append(c);
                number = -1;
            } else {
                if(number == -1) {
                    numberStart = i;
                    number = 0;
                } else if(number == 1) {
                    sb.append(c);
                    if(toPoint-- % 3 == 0)
                        sb.append(DIGIT_DELIMITER);
                }
            }
        }
        return sb.toString();
    }*/



    private TextType mTextType;
    private MenuHandler mHandler = new MenuHandler();
    private int mMaxDigitsToFit = -1;

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
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), mTextType);
    }

    private void init() {
        if(Build.VERSION.SDK_INT >= 11)
            setCustomSelectionActionModeCallback(new NoTextSelectionMode());
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        setTextType(TextType.INPUT);
    }

    public void calcMaxDigits() {
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        mMaxDigitsToFit = (int) Math.floor((getWidth() - 5) / paint.measureText("8"));
    }
    public int getMaxDigitsToFit() {
        return mMaxDigitsToFit;
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

        int select = getSelectionStart();
        setText(getEditableText().insert(select, str).toString());
        setSelection(select + str.length());
    }
    public void setText(String str) {
        setTextType(TextType.INPUT);

        //str = addDigitDelimiters(str);
        super.setText(str);
        setSelection(str.length());
    }
    /*public String getExprText() {
        return super.getText().toString().replace(new String(new char[] {DIGIT_DELIMITER}), "");
    }*/
    public void clearText() {
        super.setText("");
        setTextType(TextType.INPUT);
    }
    public void delSymbol() {
        setTextType(TextType.INPUT);

        int select = getSelectionStart();
        if(select <= 0) return;

        super.setText(getEditableText().replace(select - 1, select, ""));
        setSelection(select - 1);
    }

    public void setTextType(TextType type) {
        if(type == mTextType) return;

        if(type == TextType.INPUT) {
            setTextColor(getResources().getColor(R.color.colorInputText));
            setCursorVisible(true);

            if(mTextType == TextType.RESULT_MESSAGE)
                super.setText("");
        } else {
            setTextColor(getResources().getColor(R.color.colorEquals));
            setCursorVisible(false);
        }
        mTextType = type;
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
            }

        }
    }



    private boolean isFromMakeBlink() {
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
    }

    /*Rect r = new Rect();
    Rect r2 = new Rect();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x90FFFFFF);
        paint.setTextSize(spToPx(35));
        paint.setTextAlign(Paint.Align.LEFT);


        getLineBounds(0, r);
        //canvas.drawRect(r.right - 20, r.top, r.right, r.top + 20, paint);

        String text = getText().toString();
        for(int i = text.length() - 1; i >= 0; i--) {
            if(text.charAt(i) == DIGIT_DELIMITER || i == 3) {
                getPaint().getTextBounds(text, i, text.length(), r2);
                //canvas.drawRect(r.right - r2.width(), r.top, r.right - r2.width() + 20, r.top + 20, paint);
                canvas.drawText("’", r.right - r2.width() - spToPx(9), r.top + spToPx(29), paint);
            }
        }


        super.onDraw(canvas);
    }

    private float spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }*/

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(!hasWindowFocus())
            return true;

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // Hack to prevent keyboard and insertion handle from showing.
            cancelLongPress();

            setTextType(TextType.INPUT);

            // This is used to set cursor position for API > 10 (approximately)
            Layout layout = getLayout();
            float x = event.getX() + getScrollX();
            float y = event.getY() + getScrollY();
            int line = layout.getLineForVertical((int) y);
            int offset = layout.getOffsetForHorizontal(line, x);

            if (offset < 0) offset = 0;
            setSelection(offset);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performLongClick() {
        showContextMenu();
        return true;
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
            }
            return false;
        }

        public void copy() {
            Util.setPrimaryClip(getContext(), getText().toString());
        }
    }

    // API <= 10 will ignore that, so this error is not a big problem
    @SuppressLint("NewApi")
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

        private SavedState(Parcelable superState, TextType textType) {
            super(superState);
            mTextType = textType;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mTextType.ordinal());
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
        }
    }
}
