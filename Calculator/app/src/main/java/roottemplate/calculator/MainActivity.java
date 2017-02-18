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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Random;

import roottemplate.calculator.data.AppDatabase;
import roottemplate.calculator.data.KeyboardKits;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.evaluator.util.ExpressionFormatUpdater;
import roottemplate.calculator.util.ParcelableBinder;
import roottemplate.calculator.util.Util;
import roottemplate.calculator.view.FirstLaunchDialogFragment;
import roottemplate.calculator.view.InputEditText;
import roottemplate.calculator.view.KitViewPager;
import roottemplate.calculator.view.NotifyDialogFragment;
import roottemplate.calculator.view.ShiftButton;
import roottemplate.calculator.view.SystemButton;
import roottemplate.calculator.view.UpdateDialogFragment;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    private static final int REQUEST_CODE_HISTORY = 0;
    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final int REQUEST_CODE_GUIDES = 2;

    private static final int GROUP_KIT_MAGIC = 11;


    private KeyboardKits mKeyboardKits;
    private KeyboardKits.KitVersion mCurrentKeyboardKitVersion;

    private PreferencesManager mPrefs;
    private EvaluatorManager mEvalManager;
    private AppDatabase mDatabase;
    private int mTipsPageIndex;
    private Messenger mActivityMessenger = null;

    private InputEditText mInputText;
    private KitViewPager mViewPager;

    private View mLastClickedView = null;
    private long mLastClickedTime = -1;

    private Thread mHistoryClearingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = new PreferencesManager(this);
        int dayNightTheme = mPrefs.dayNightTheme();
        //noinspection WrongConstant
        AppCompatDelegate.setDefaultNightMode(PreferencesManager.dayNightThemeIdToMode(this, dayNightTheme));
        if(dayNightTheme == PreferencesManager.THEME_LEGACY)
            setTheme(R.style.AppTheme_Legacy_NoActionBar);

        super.onCreate(savedInstanceState);

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
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // TODO: normal margin fix
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    findViewById(R.id.inputTextParent).getLayoutParams();
            params.topMargin = 0;
            params.leftMargin = 0;
            params = (LinearLayout.LayoutParams) findViewById(R.id.activity_main_del).getLayoutParams();
            params.topMargin = 0;
            params.rightMargin = 0;
        }

        // INIT KEYBOARD KITS
        if(!readKeyboardKits())
            return;

        // INIT ACTION BAR (after INIT KEYBOARD KITS as mKeyboardKits is used in actionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getWindowManager().getDefaultDisplay().getHeight() < 500) {
            getSupportActionBar().hide();
        }

        // INIT DATABASES
        mDatabase = new AppDatabase(this, mPrefs);
        if(mPrefs.enabledHistory()) {
            mDatabase.getHistory().updateDatabase(savedInstanceState == null, true);
            mHistoryClearingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(5 * 60 * 1000);
                        } catch (InterruptedException e) {
                            return;
                        }
                        mDatabase.getHistory().updateDatabase(false, false);
                    }
                }
            });
        }

        // INIT NAMESPACE
        FragmentManager fm = getSupportFragmentManager();
        EvaluatorManager.NamespaceFragment nsFragment = (EvaluatorManager.NamespaceFragment)
                fm.findFragmentByTag(EvaluatorManager.NamespaceFragment.FRAGMENT_NAME);
        if (nsFragment == null) {
            nsFragment = new EvaluatorManager.NamespaceFragment();
            fm.beginTransaction().add(nsFragment, EvaluatorManager.NamespaceFragment.FRAGMENT_NAME)
                    .commit();
        }
        mEvalManager = new EvaluatorManager(this, nsFragment, mPrefs, mDatabase, savedInstanceState == null);

        // INIT KitViewPager
        mViewPager = (KitViewPager) findViewById(R.id.activity_main_viewPager);
        invalidateCurrentKeyboardKit();

        // UPDATE VERSION
        updateVersion();
        if(UpdateDialogFragment.shouldAskForUpdate(mPrefs)) {
            new UpdateDialogFragment().show(getSupportFragmentManager(),
                    UpdateDialogFragment.FRAGMENT_TAG);
        }

        // MISCELLANEOUS INITIALIZATIONS
        mTipsPageIndex = savedInstanceState == null ? 0 : savedInstanceState.getInt("tipsPageIndex");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mEvalManager != null) { // Can be false if readKeyboardKits() went unsuccessful
            mEvalManager.updateEvaluatorOptions(); // In case if they have just been changed in settings
            mInputText.initDigitFormatting(mPrefs.digitGrouping(), mPrefs.digitSeparatorLeft(),
                    mPrefs.digitSeparatorFract()/*, mPrefs.highlightE()*/);
        }
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
            mDatabase.close();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            mInputText.calcMaxDigits();
            invalidateActionBarMenu();
        }
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
            if(data.getBooleanExtra("clearAllNamespaces", false)) {
                mEvalManager.clearAllNamespace();
                if(mInputText.getTextType() == InputEditText.TextType.RESULT_MESSAGE)
                    mInputText.clearText();
            }
            if(data.getBooleanExtra("updateSelectedKit", false)) {
                mEvalManager.invalidateEvaluator();
                // Setting the same to cause Evaluator updating
                if(mInputText.getTextType() == InputEditText.TextType.RESULT_MESSAGE)
                    mInputText.clearText();
            }
        } else if(requestCode == REQUEST_CODE_GUIDES) {
            mTipsPageIndex = data.getIntExtra("guideIndex", 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        KeyboardKits.Kit[] kits = mKeyboardKits.mKits;
        if(kits.length >= 2) {
            Random random = new Random();
            for(int i = 0; i < kits.length; i++) {
                KeyboardKits.Kit kit = kits[i];
                if(kit.mActionBarAccess) {
                    MenuItem item = menu.add(GROUP_KIT_MAGIC, random.nextInt(), i, kit.mShortName);
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
        // as you specify a mParent activity in AndroidManifest.xml.
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
            int i = 0;
            String[] kitNames = new String[mKeyboardKits.mKits.length * 2];
            for(KeyboardKits.Kit kit : mKeyboardKits.mKits) {
                kitNames[i++] = kit.mName;
                kitNames[i++] = kit.mShortName;
            }
            startActivityForResult(
                    new Intent(this, SettingsActivity.class)
                            .putExtra("historySize", mDatabase.getHistory().getElementCount())
                            .putExtra("kitNames", kitNames),
                    REQUEST_CODE_SETTINGS
            );
            return true;
        } else if(id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        if(item.getGroupId() == GROUP_KIT_MAGIC) {
            mPrefs.kitName(mKeyboardKits.mKits[item.getOrder()].mName);
            invalidateCurrentKeyboardKit();
        }

        return super.onOptionsItemSelected(item);
    }


    // LOGIC METHODS

    public KeyboardKits getKeyboardKits() {
        return mKeyboardKits;
    }

    public KeyboardKits.KitVersion getPreferredKeyboardKitVersion() {
        return mCurrentKeyboardKitVersion;
    }

    public Messenger getMessenger() {
        if(mActivityMessenger == null) {
            mActivityMessenger = new Messenger(new ActivityHandler(this));
        }
        return mActivityMessenger;
    }

    public PreferencesManager getPrefs() {
        return mPrefs;
    }

    public int getViewPagerCurrentItem() {
        return mViewPager.getCurrentItem();
    }
    public void scrollViewPagerTo(int item) {
        mViewPager.setCurrentItem(item);
    }


    private boolean readKeyboardKits() {
        try {
            mKeyboardKits = KeyboardKitsXmlManager.parse(this);
            if (mKeyboardKits.mKits.length == 0) {
                Log.e(Util.LOG_TAG, "ButtonKits xml file has 0 kits. Restoring default xml");
                KeyboardKitsXmlManager.restoreDefaultButtonKitsXml(this);
                mKeyboardKits = KeyboardKitsXmlManager.parse(this);
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(Util.LOG_TAG, "Exception while parsing ButtonKits. Restoring default xml", e);
            Util.fatalError(this, R.string.message_bad_kits_xml, e);
            return false;
        }
        return true;
    }

    private void updateVersion() {
        int latestVer = Util.getAppVersion(this), thisVer = mPrefs.version();
        if(latestVer == -1) return; // Error
        if(latestVer == thisVer) return;
        UpdateDialogFragment.onAppUpdated(mPrefs);
        boolean hasNewKKits = latestVer == 2 || latestVer == 3;

        if(thisVer == -1) {
            new FirstLaunchDialogFragment().show(getSupportFragmentManager(),
                    FirstLaunchDialogFragment.FRAGMENT_TAG);
        } else if(hasNewKKits) {
            if(mKeyboardKits.mIsDefault) {
                NotifyDialogFragment dialog = new NotifyDialogFragment();
                Bundle args = new Bundle();
                args.putInt("title", R.string.dialog_kitsUpdated_title);
                args.putInt("message", R.string.dialog_kitsUpdated_message);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "KeyboardKitsUpdated");

                KeyboardKitsXmlManager.invalidateInstalledKeyboardKits(this);
                readKeyboardKits();
                invalidateCurrentKeyboardKit();
            } else {
                /* TODO. Update KeyboardKits if new version provides new default kits.
                    If user has already specified custom ButtonKits, ask to try new default;
                    also provide button in options menu to roll back to custom.
                 */
            }
        }
        mPrefs.version(latestVer);
    }

    public void invalidateCurrentKeyboardKit() {
        KeyboardKits.KitVersion kitVersion = KeyboardKitsXmlManager.getPreferredKitVersion(mKeyboardKits.mKits,
                mPrefs.kitName(), getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if(kitVersion == mCurrentKeyboardKitVersion) return;
        mCurrentKeyboardKitVersion = kitVersion;
        mPrefs.kitName(mCurrentKeyboardKitVersion.mParent.mName); // To set the kit name if it was null

        mViewPager.init(getSupportFragmentManager(),
                kitVersion.mPages.length,
                kitVersion.mMainPageIndex);
        mEvalManager.setKit(kitVersion.mParent.mName);

        if(mPrefs.separateNamespace())
            mInputText.clearText();

        invalidateActionBarMenu();
    }

    private void invalidateActionBarMenu() {
        // Hacks to set selected MenuItem bold
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        KeyboardKits.Kit[] kits = mKeyboardKits.mKits;
        KeyboardKits.Kit currentKit = mCurrentKeyboardKitVersion.mParent;
        if(kits.length < 2) return;

        ActionMenuView menuView = null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof ActionMenuView) {
                menuView = (ActionMenuView) view;
                break;
            }
        }

        Menu menu = toolbar.getMenu();
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if(item.getGroupId() == GROUP_KIT_MAGIC) {
                boolean typefaceSet = false;
                int typeface = (kits[item.getOrder()] == currentKit) ? Typeface.BOLD : Typeface.NORMAL;

                // For buttons in the action bar
                if(menuView != null && i < menuView.getChildCount()) {
                    View itemView = menuView.getChildAt(i);
                    if(itemView instanceof ActionMenuItemView) {
                        ((ActionMenuItemView) itemView).setTypeface(null, typeface);
                        typefaceSet = true;
                    }
                }

                // For items in the overflow menu
                if(!typefaceSet) {
                    SpannableString s = new SpannableString(item.getTitle().toString());
                    s.setSpan(new StyleSpan(typeface), 0, s.length(), 0);
                    item.setTitle(s);
                }
            }
        }
    }

    public void invalidateEvaluatorOptions() {
        mEvalManager.updateEvaluatorOptions();
    }

    public void showTips() {
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtra("guideIndex", mTipsPageIndex);
        intent.putExtra("messengerBinder", new ParcelableBinder(getMessenger().getBinder()));
        intent.putExtra("pagesCount", mCurrentKeyboardKitVersion.mPages.length);
        intent.putExtra("pagesCurrent", mViewPager.getCurrentItem());
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

    public void onShiftButtonClick(View view) {
        mViewPager.getCurrentPageFragment().onShiftButtonClicked();
    }

    public void onSystemButtonClick(View view) {
        mViewPager.getCurrentPageFragment().onSystemButtonClick(((SystemButton) view).getProperty());
    }

    public void onPadButtonClick(View view) {
        onPadButtonClick(view, false);
    }

    public void onPadButtonClick(View view, boolean inverseTextCase) {
        KeyboardKits.Button btn = mKeyboardKits.mButtons[(int) view.getTag()];
        if (btn.mType == KeyboardKits.ButtonType.EQUALS) {
            eval();
            return;
        }

        KeyboardKits.PageReturnType returnType =
                mCurrentKeyboardKitVersion.mPages[mViewPager.getCurrentItem()].mMoveToMain;
        long time = System.currentTimeMillis();
        boolean doubleClick = (view == mLastClickedView && (time - mLastClickedTime) <= 400);
        boolean returnByDoubleClick = (returnType == KeyboardKits.PageReturnType.IF_DOUBLE_CLICK && doubleClick);
        mLastClickedView = view;
        mLastClickedTime = time;
        // TODO: icon, edit keyboard kits button, as fractional (?)
        // TODO: tips <- (double clicks on shift ?, asin = arcsin ?, editing keyboard kits,
        //       namespaces in keyboard kits)
        // TODO: delete configChanges="..." from manifest when this bug will be fixed. Links:
        //       https://code.google.com/p/android/issues/detail?id=206394
        //       https://code.google.com/p/android/issues/detail?id=225911
        /* DONE:
         *    history repeating
         *    fix input field
         *    error - do not display in input field
         *    tips <- scroll bar show on move
         *    sin/cos move in keyboard kits
         *    DEG/RAD
         *    fixed bug: f(x) incorrect output in Edit Namespace activity
         *    shift disabled after double click
         *    designs
         *    update checker
         *    tips updated
         *    Copyright year updated
         */

        KitViewPager.PageFragment fragment = mViewPager.getCurrentPageFragment();
        if(!returnByDoubleClick) {
            if (mInputText.getTextType() != InputEditText.TextType.INPUT &&
                    btn.mType == KeyboardKits.ButtonType.DIGIT)
                mInputText.clearText();

            if(fragment.onPadButtonClicked() != ShiftButton.STATE_DISABLED)
                inverseTextCase = !inverseTextCase;
            String str = inverseTextCase ? Util.inverseTextCase(btn.mText) : btn.mText;
            //String suffix = mPrefs.autoBracketClosing() ? Util.generateClosingBrackets(str) : null;
            mInputText.appendText(str, false/*, suffix*/);
        } else
            fragment.setCurrentShiftState(ShiftButton.STATE_DISABLED);
            // To set shift state from CAPSLOCK to DISABLED

        if(returnType == KeyboardKits.PageReturnType.ALWAYS || returnByDoubleClick)
            mViewPager.setCurrentItem(mCurrentKeyboardKitVersion.mMainPageIndex);
    }

    private void eval() {
        if(mInputText.getTextType() != InputEditText.TextType.INPUT) return; // Already evaluated
        String text = mInputText.getExprText();
        if(text.isEmpty()) return;

        EvaluatorManager.EvalResult res = mEvalManager.eval(text, mInputText.getMaxDigitsToFit());
        if(res.mText != null && res.mMessage == null)
            mInputText.setText(res.mText);
        if(res.mTextType != null)
            mInputText.setTextType(res.mTextType);
        if(res.mMessage != null) {
            Snackbar.make(findViewById(R.id.coordinator), res.mText + "\n" + res.mMessage,
                    Snackbar.LENGTH_SHORT).show();
            if(res.mErrorIndex != -1)
                mInputText.setCursor(res.mErrorIndex + 1);
        }
    }


    public static class ActivityHandler extends Handler {
        public static final int MSG_PAGE_SCROLL_TO = 0;

        private WeakReference<MainActivity> mActivity;
        private ActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity a = mActivity.get();
            if(a == null) return;

            switch(msg.what) {
                case MSG_PAGE_SCROLL_TO:
                    a.scrollViewPagerTo(msg.arg1);
                    break;
            }
        }
    }
}
