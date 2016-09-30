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

package roottemplate.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import roottemplate.calculator.util.ParcelableBinder;
import roottemplate.calculator.util.Util;

public class GuideActivity extends AppCompatActivity {
    private static final int[] GUIDES = new int[]{
            R.id.guide_empty,
            R.id.guide_pads, // PADS
            R.id.guide_arOrder, // OPERATOR ORDER
            R.id.guide_empty, R.id.guide_same_button, R.id.guide_same_button, R.id.guide_same_button, // SAME BUTTONS
            R.id.guide_log, R.id.guide_root, // log, root
            R.id.guide_nan, // NaN
            R.id.guide_longClks, // LONG CLICKS ON BUTTONS
            R.id.guide_empty, // DOUBLE CLICKS TO MOVE TO MAIN
            R.id.guide_historyClks, // HISTORY CLICKS
            R.id.guide_empty, // SETTINGS IS YOUR FRIEND
            R.id.guide_final
    };

    private int mCurGuideIndex = -1;
    private Intent mResult;

    private Messenger mMainActivity;
    private int mPageCurrent;
    private int mPageCount;

    private ScrollView mScroll;
    private TextView mTitle;
    private TextView mText;

    private Button mSameButtonBtn0, mSameButtonBtn1, mSameButtonBtn2, mSameButtonBtn3;
    private TextView mSameButtonText1, mSameButtonText2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = getWindow();
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams();
        layout.copyFrom(window.getAttributes());
        layout.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //layout.height = Math.round(((WindowManager) getSystemService(Context.WINDOW_SERVICE))
        //        .getDefaultDisplay().getWidth() * 1F);
        window.setAttributes(layout);

        Intent intent = getIntent();
        mMainActivity = new Messenger((
                (ParcelableBinder) intent.getParcelableExtra("messengerBinder")).getBinder());
        mPageCurrent = intent.getIntExtra("pagesCurrent", 0);
        mPageCount = intent.getIntExtra("pagesCount", 0);

        mScroll = (ScrollView) findViewById(R.id.scrollView);
        mTitle = (TextView) findViewById(R.id.guide_title);
        mText = (TextView) findViewById(R.id.guide_text);

        mSameButtonBtn0 = (Button) findViewById(R.id.guide_same_button_btn0);
        mSameButtonBtn1 = (Button) findViewById(R.id.guide_same_button_btn1);
        mSameButtonBtn2 = (Button) findViewById(R.id.guide_same_button_btn2);
        mSameButtonBtn3 = (Button) findViewById(R.id.guide_same_button_btn3);
        mSameButtonText1 = (TextView) findViewById(R.id.guide_same_button_text1);
        mSameButtonText2 = (TextView) findViewById(R.id.guide_same_button_text2);

        View.OnLongClickListener longClkLongListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onGuideLongClkClick(v, true);
                return true;
            }
        };
        findViewById(R.id.guide_longClks_btn1).setOnLongClickListener(longClkLongListener);
        findViewById(R.id.guide_longClks_btn2).setOnLongClickListener(longClkLongListener);

        mResult = new Intent();
        setGuideIndex(savedInstanceState == null ? intent.getIntExtra("guideIndex", 0) :
                savedInstanceState.getInt("guideIndex"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("guideIndex", mCurGuideIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guide, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.action_guide_left:
                    item.setEnabled(mCurGuideIndex > 0);
                    break;
                case R.id.action_guide_right:
                    item.setEnabled(mCurGuideIndex < GUIDES.length - 1);
                    break;
                case R.id.action_guide_page:
                    item.setTitle((mCurGuideIndex + 1) + " / " + GUIDES.length);
                    break;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_guide_left:
                setGuideIndex(mCurGuideIndex - 1);
                return true;
            case R.id.action_guide_right:
                setGuideIndex(mCurGuideIndex + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // LOGIC METHODS

    public void onPadButtonClick(View view) {
    }

    private void setGuideIndex(int index) {
        if (mCurGuideIndex == index) return;
        if(index < 0) index = 0;
        else if(index >= GUIDES.length) index = GUIDES.length - 1;

        if(mCurGuideIndex == -1) {
            findViewById(GUIDES[index]).setVisibility(View.VISIBLE);
        } else if (GUIDES[mCurGuideIndex] != GUIDES[index]) {
            findViewById(GUIDES[mCurGuideIndex]).setVisibility(View.GONE);
            findViewById(GUIDES[index]).setVisibility(View.VISIBLE);
        }

        switch (index) {
            case 0:
                mTitle.setText(R.string.guide_1_title);
                mText.setText(R.string.guide_1_text);
                break;
            case 1:
                mTitle.setText(R.string.guide_2_title);
                mText.setText(R.string.guide_2_text);
                updateScrollPageButtons();
                break;
            case 2:
                mTitle.setText(R.string.guide_7_title);
                mText.setText(R.string.guide_7_text);
                break;
            case 3:
                mTitle.setText(R.string.guide_3_title);
                mText.setText(R.string.guide_3_text);
                break;
            case 4:
                setupGuideSameButton(R.string.guide_4_title, 1, "=", "=", R.string.guide_4_equals, R.string.guide_4_system);
                break;
            case 5:
                setupGuideSameButton(R.string.guide_5_title, 2, "E", "e", R.string.guide_5_E, R.string.guide_5_e);
                break;
            case 6:
                setupGuideSameButton(R.string.guide_6_title, 0, ".", ",", R.string.guide_6_point, R.string.guide_6_comma);
                break;
            case 7:
                mTitle.setText(R.string.guide_8_title);
                mText.setText(R.string.guide_8_text1);
                ((TextView) findViewById(R.id.guide_log_math2)).setText(Html.fromHtml("log(b, n) = log<sub>b</sub> n"));
                break;
            case 8:
                mTitle.setText(R.string.guide_9_title);
                mText.setText(R.string.guide_9_text1);
                ((TextView) findViewById(R.id.guide_root_math)).setText(Html.fromHtml("√(4, 16) = <sup>4</sup>√16 = 2"));
                break;
            case 9:
                mTitle.setText(R.string.guide_10_title);
                mText.setText(R.string.guide_10_text);
                break;
            case 10:
                mTitle.setText(R.string.guide_11_title);
                mText.setText(R.string.guide_11_text);
                break;
            case 11:
                mTitle.setText(R.string.guide_15_title);
                mText.setText(R.string.guide_15_text);
                break;
            case 12:
                mTitle.setText(R.string.guide_12_title);
                mText.setText(R.string.guide_12_text);
                break;
            case 12 + 1:
                mTitle.setText(R.string.guide_14_title);
                mText.setText(R.string.guide_14_text);
                break;
            case 14:
                mTitle.setText(R.string.guide_final_title);
                mText.setText("");
                break;
        }

        mScroll.scrollTo(0, 0);
        mCurGuideIndex = index;
        invalidateOptionsMenu();
        setResult(RESULT_OK, mResult.putExtra("guideIndex", mCurGuideIndex));
    }


    private void setupGuideSameButton(int title, int btnIndex, String leftBtn, String rightBtn,
                                      int leftText, int rightText) {
        mTitle.setText(title);
        mText.setText("");
        findViewById(R.id.guide_same_button_btn0_parent).setVisibility(btnIndex == 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.guide_same_button_btn1_parent).setVisibility(btnIndex == 1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.guide_same_button_btn2_parent).setVisibility(btnIndex == 2 ? View.VISIBLE : View.GONE);
        switch (btnIndex) {
            case 0: mSameButtonBtn0.setText(leftBtn); break;
            case 1: mSameButtonBtn1.setText(leftBtn); break;
            case 2: mSameButtonBtn2.setText(leftBtn); break;
        }
        mSameButtonBtn3.setText(rightBtn);
        mSameButtonText1.setText(Html.fromHtml(getResources().getString(leftText)));
        mSameButtonText1.setMovementMethod(LinkMovementMethod.getInstance());
        mSameButtonText2.setText(rightText);
    }


    public void onGuideLongClkClick(View view) {
        onGuideLongClkClick(view, false);
    }
    private void onGuideLongClkClick(View view, boolean isLong) {
        int textId = view.getId() == R.id.guide_longClks_btn1 ?
                R.id.guide_longClks_res1 : R.id.guide_longClks_res2;
        String inner = ((Button) view).getText().toString();
        String outer = isLong ? Util.inverseTextCase(inner) : inner;
        ((TextView) findViewById(textId)).setText(outer);
    }

    public void onOpenHistory(View view) {
        PreferencesManager prefs = new PreferencesManager(PreferenceManager.getDefaultSharedPreferences(this),
                getResources());
        HistoryDatabase db = new HistoryDatabase(this, prefs);
        if(db.getElementCount() == 0) {
            db.addHistoryElement("1−2", "-1", null);
            db.addHistoryElement("x=5", null, null);
            db.addHistoryElement("2++1", getResources().getString(R.string.error), "Unknown symbol: '+'");
        }
        startActivity(new Intent(this, HistoryActivity.class));
    }

    public void onPagesScrollLeft(View view) {
        scrollPages(-1);
    }
    public void onPagesScrollRight(View view) {
        scrollPages(1);
    }

    private void scrollPages(int delta) {
        try {
            Message msg = Message.obtain();
            msg.what = MainActivity.ActivityHandler.MSG_PAGE_SCROLL_TO;
            msg.arg1 = mPageCurrent = mPageCurrent + delta;
            mMainActivity.send(msg);
            updateScrollPageButtons();
        } catch (RemoteException e) {
            Log.w(Util.LOG_TAG, "MainActivity has no instance", e);
        }
    }

    private void updateScrollPageButtons() {
        findViewById(R.id.guide_pads_left).setEnabled(mPageCurrent > 0);
        findViewById(R.id.guide_pads_right).setEnabled(mPageCurrent < mPageCount - 1);
    }
}