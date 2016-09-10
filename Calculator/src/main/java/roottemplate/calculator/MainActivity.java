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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import roottemplate.calculator.evaluator.Evaluator;
import roottemplate.calculator.evaluator.EvaluatorException;
import roottemplate.calculator.pads.ButtonKits;
import roottemplate.calculator.pads.ButtonKitsXmlFile;
import roottemplate.calculator.util.EvaluatorBridge;
import roottemplate.calculator.util.FirstLaunchDialogFragment;
import roottemplate.calculator.util.Util;
import roottemplate.calculator.view.KitViewPager;
import roottemplate.calculator.view.InputEditText;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    private static final int REQUEST_CODE_HISTORY = 0;
    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final int REQUEST_CODE_GUIDES = 2;

    private static final int GROUP_KIT_MAGIC = 11;

    private static MainActivity mainActivity;

    private Data mData;
    private ButtonKits mButtonKits;
    private ButtonKits.KitVersion mCurrentButtonKitVersion;
    private PreferencesManager mPrefs;
    private HistoryDatabase mHistory;
    private int mTipsPageIndex;

    private InputEditText mInputText;
    private KitViewPager mViewPager;

    private View mLastClickedView = null;
    private long mLastClickedTime = -1;

    private Thread mHistoryClearingThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);

        // INIT Data FRAGMENT
        FragmentManager fm = getSupportFragmentManager();
        mData = (Data) fm.findFragmentByTag("data");
        if (mData == null) {
            mData = new Data();
            fm.beginTransaction().add(mData, "data").commit();
        }
        mPrefs = new PreferencesManager(PreferenceManager.getDefaultSharedPreferences(this), getResources());

        if(mData.mEvaluator == null) {
            mData.mEvaluator = new Evaluator();
            mData.mEvaluator.options.ENABLE_HASH_COMMANDS = false;
        }

        // SET CONTENT VIEW
        setContentView(R.layout.activity_main);
        mInputText = (InputEditText) findViewById(R.id.activity_main_expr);
        findViewById(R.id.activity_main_del).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onDelButtonLongClick();
                return true;
            }
        });

        // INIT BUTTON KITS
        try {
            mButtonKits = ButtonKitsXmlFile.parse(this);
            if (mButtonKits.mKits.length == 0) {
                Log.e(Util.LOG_TAG, "ButtonKits xml file has 0 kits. Restoring default xml");
                ButtonKitsXmlFile.restoreDefaultButtonKitsXml(this);
                mButtonKits = ButtonKitsXmlFile.parse(this);
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(Util.LOG_TAG, "Exception while parsing ButtonKits. Restoring default xml", e);
            Util.fatalError(this, R.string.message_bad_kits_xml, e);
            return;
        }

        // INIT ACTION BAR (after INIT BUTTON KITS as mButtonKits is used in actionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getWindowManager().getDefaultDisplay().getHeight() < 500) {
            getSupportActionBar().hide();
        }

        // INIT KitViewPager
        mViewPager = (KitViewPager) findViewById(R.id.activity_main_viewPager);
        invalidateCurrentButtonKit();

        // INIT HISTORY DATABASE IF NEEDED
        if(mPrefs.enabledHistory()) {
            mHistory = new HistoryDatabase(this, mPrefs);
            mHistory.updateDatabase(savedInstanceState == null, true);
            mHistoryClearingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(5 * 60 * 1000);
                        } catch (InterruptedException e) {
                            return;
                        }
                        mHistory.updateDatabase(false, false);
                    }
                }
            });
        }

        // UPDATE VERSION
        updateVersion();

        // MISCELLANEOUS INITIALIZATIONS
        mTipsPageIndex = savedInstanceState == null ? 0 : savedInstanceState.getInt("tipsPageIndex");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mData.mEvaluator.options.ANGLE_MEASURING_UNITS = mPrefs.getAMU();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tipsPageIndex", mTipsPageIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mPrefs.enabledHistory()) {
            if(mHistoryClearingThread != null)
                mHistoryClearingThread.interrupt();
            if(mHistory != null)
                mHistory.close();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus)
            mInputText.calcMaxDigits();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_HISTORY && resultCode == RESULT_OK) {
            String paste = data.getStringExtra("paste");
            if(paste != null) {
                mInputText.appendText(paste, true);
            }
        } else if(requestCode == REQUEST_CODE_SETTINGS) {
            if(data.getBooleanExtra("clearNamespace", false))
                mData.mEvaluator.clear();
        } else if(requestCode == REQUEST_CODE_GUIDES) {
            mTipsPageIndex = data.getIntExtra("guideIndex", 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ButtonKits.Kit[] kits = mButtonKits.mKits;
        if(kits.length >= 2) {
            for(int i = 0; i < kits.length; i++) {
                ButtonKits.Kit kit = kits[i];
                if(kit.mActionBarAccess) {
                    MenuItem item = menu.add(GROUP_KIT_MAGIC, Menu.NONE, i, kit.mShortName);
                    MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if(item.getItemId() == R.id.action_history)
                item.setVisible(mPrefs.enabledHistory());
            else if(item.getItemId() == R.id.action_guide)
                item.setVisible(mPrefs.enabledTips());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivityForResult(intent, REQUEST_CODE_HISTORY);
            return true;
        } else if(id == R.id.action_guide) {
            showTips();
            return true;
        } else if (id == R.id.action_settings) {
            startActivityForResult(
                    new Intent(this, SettingsActivity.class)
                            .putExtra("historySize", mHistory.getElementCount()),
                    REQUEST_CODE_SETTINGS
            );
            return true;
        } else if(id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        if(item.getGroupId() == GROUP_KIT_MAGIC) {
            mPrefs.kitName(mButtonKits.mKits[item.getOrder()].mName);
            invalidateCurrentButtonKit();
        }

        return super.onOptionsItemSelected(item);
    }


    // LOGIC METHODS

    public static MainActivity instance() {
        return mainActivity;
    }

    public Data getData() {
        return mData;
    }

    public ButtonKits getButtonKits() {
        return mButtonKits;
    }

    public ButtonKits.KitVersion getPreferredButtonKitVersion() {
        return mCurrentButtonKitVersion;
    }

    public int getViewPagerCurrentItem() {
        return mViewPager.getCurrentItem();
    }
    public void scrollViewPagerTo(int item) {
        mViewPager.setCurrentItem(item);
    }


    private void updateVersion() {
        int latestVer, thisVer = mPrefs.version();
        try {
            latestVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Util.LOG_TAG, "Unable to get version", e);
            return;
        }
        if(latestVer == thisVer) return;

        if(thisVer == -1) {
            new FirstLaunchDialogFragment().show(getSupportFragmentManager(),
                    FirstLaunchDialogFragment.FRAGMENT_TAG);
        } else {
            /* TODO. Update ButtonKits if new version provides new default kits.
                If user has already specified custom ButtonKits, ask to try new default;
                also provide button in options menu to roll back to custom.
             */
        }
        mPrefs.version(latestVer);
    }

    public void invalidateCurrentButtonKit() {
        ButtonKits.KitVersion kitVersion = ButtonKitsXmlFile.getPreferredKitVersion(mButtonKits.mKits,
                mPrefs.kitName(), getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if(kitVersion == mCurrentButtonKitVersion) return;
        mCurrentButtonKitVersion = kitVersion;

        mViewPager.init(getSupportFragmentManager(),
                kitVersion.mPages.length,
                kitVersion.mMainPageIndex);
    }

    public void showTips() {
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtra("guideIndex", mTipsPageIndex);
        startActivityForResult(intent, REQUEST_CODE_GUIDES);
    }

    public void onDelButtonClick(View view) {
        if(mInputText.getTextType() == InputEditText.TextType.RESULT_NUMBER)
            mInputText.clearText();
        else
            mInputText.delSymbol();
    }
    public void onDelButtonLongClick() {
        mInputText.clearText();
    }

    @Override
    public boolean onLongClick(View v) {
        onPadButtonClick(v, true);
        return true;
    }

    public void onPadButtonClick(View view) {
        onPadButtonClick(view, false);
    }

    public void onPadButtonClick(View view, boolean inverseTextCase) {
        ButtonKits.Button btn = mButtonKits.mButtons[(int) view.getTag()];
        if (btn.mType == ButtonKits.ButtonType.EQUALS) {
            eval();
            return;
        }

        ButtonKits.PageReturnType returnType =
                mCurrentButtonKitVersion.mPages[mViewPager.getCurrentItem()].mMoveToMain;
        long time = System.currentTimeMillis();
        boolean doubleClick = (view == mLastClickedView && (time - mLastClickedTime) <= 400);
        boolean returnByDoubleClick = (returnType == ButtonKits.PageReturnType.IF_DOUBLE_CLICK && doubleClick);
        mLastClickedView = view;
        mLastClickedTime = time;

        if(!returnByDoubleClick) {
            if (mInputText.getTextType() != InputEditText.TextType.INPUT &&
                    btn.mType == ButtonKits.ButtonType.DIGIT)
                mInputText.clearText();
            mInputText.appendText(inverseTextCase ? Util.inverseTextCase(btn.mText) : btn.mText,
                    false);
        }

        if(returnType == ButtonKits.PageReturnType.ALWAYS || returnByDoubleClick)
            mViewPager.setCurrentItem(mCurrentButtonKitVersion.mMainPageIndex);
    }

    private void eval() {
        if(mInputText.getTextType() != InputEditText.TextType.INPUT) return; // Already evaluated
        String text = mInputText.getText().toString();
        if(text.isEmpty()) return;

        text = EvaluatorBridge.closeUnclosedBrackets(text, mPrefs.bracketClosingType());
        String result, error = null;
        InputEditText.TextType type;
        boolean historyRightIsNull = false;

        try {
            roottemplate.calculator.evaluator.Number n = EvaluatorBridge.eval(mData.mEvaluator, text);

            if (n != null) {
                if(mPrefs.doRound()) {
                    result = EvaluatorBridge.doubleToString(n.doubleValue(),
                            mInputText.getMaxDigitsToFit());
                } else {
                    result = Double.toString(n.doubleValue());
                }
                result = EvaluatorBridge.replaceEngineToApp(result); // For Infinity -> symbol

                type = InputEditText.TextType.RESULT_NUMBER;
            } else {
                type = InputEditText.TextType.RESULT_MESSAGE;
                result = mInputText.getText().toString();
                historyRightIsNull = true;
            }
        } catch (EvaluatorException e) {
            error = EvaluatorBridge.replaceEngineToApp(e.getLocalizedMessage());
            Snackbar.make(findViewById(R.id.coordinator), error, Snackbar.LENGTH_LONG).show();
            result = getResources().getString(R.string.error);
            type = InputEditText.TextType.RESULT_MESSAGE;
        }

        mInputText.setText(result);
        mInputText.setTextType(type);

        if(mPrefs.enabledHistory()) {
            mHistory.addHistoryElement(text, historyRightIsNull ? null : result, error);
        }
    }



    public static class Data extends Fragment {
        public Evaluator mEvaluator = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
