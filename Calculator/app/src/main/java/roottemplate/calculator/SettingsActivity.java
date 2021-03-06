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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import roottemplate.calculator.data.AppDatabase;
import roottemplate.calculator.data.KeyboardKitsXmlManager;
import roottemplate.calculator.util.AppCompatPreferenceActivity;
import roottemplate.calculator.view.IfDialogFragment;
import roottemplate.calculator.view.OnDialogResultListener;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements OnDialogResultListener {
    private static final int REQUEST_EDIT_NAMESPACE = 0;
    private static final int REQUEST_EDIT_KEYBOARD_KITS = 1;
    private static final int DIALOG_CLEAR_NAMESPACES = 0;

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private BindPreferenceChangeListener sBindPreferenceSummaryToValueListener = new BindPreferenceChangeListener();

    private class BindPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        public void onPreferenceChange(Preference preference, Object value, boolean initialCall) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            switch (preference.getKey()) {
                case "outputType":
                    if(!initialCall) mResultIntent.putExtra("outputTypeChanged", true);
                case "digitGrouping":
                case "digitGroupingSeparatorLeft":
                case "digitGroupingSeparatorFract":
                    if(!initialCall) onInputDigitGroupingChanged();
                    break;
                case "dayNightTheme":
                    if(!initialCall) onThemeValueChanged();
                    break;
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            onPreferenceChange(preference, newValue, false);
            return true;
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""), true);
    }

    private Intent mResultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        mResultIntent = new Intent();
        mResultIntent.putExtra("historyCleared", false);
        setResult(-1, mResultIntent);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Fix of annoying top/bottom padding in landscape orientation
            // Issue noted on phones (android versions 4 - 6)
            ViewGroup parent = (ViewGroup) getListView().getParent();
            parent.setPadding(parent.getPaddingLeft(), 0, parent.getPaddingRight(), 0);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupClearPreference(String prefName, final boolean clearedByDefault,
                                      final int clearedSummary, final Runnable onClearListener) {
        Preference pref = findPreference(prefName);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(!clearedByDefault)
                    onClearListener.run(); // Called from line below

                preference.setEnabled(false);
                preference.setSummary(clearedSummary);
                return true;
            }
        });
        if(clearedByDefault)
            pref.getOnPreferenceClickListener().onPreferenceClick(pref);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        /*// Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);*/

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("amu"));
        bindPreferenceSummaryToValue(findPreference("bracketClosingType"));
        bindPreferenceSummaryToValue(findPreference("storingHistory"));
        bindPreferenceSummaryToValue(findPreference("digitGroupingSeparatorLeft"));
        bindPreferenceSummaryToValue(findPreference("digitGroupingSeparatorFract"));
        bindPreferenceSummaryToValue(findPreference("outputType"));
        bindPreferenceSummaryToValue(findPreference("storingNamespace"));
        bindPreferenceSummaryToValue(findPreference("dayNightTheme"));

        findPreference("highlightE").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                onInputDigitGroupingChanged();
                return true;
            }
        });

        setupClearPreference("clearHistory", getIntent().getIntExtra("historySize", -1) == 0,
                R.string.pref_clearHistory_empty, new Runnable() {
                    @Override
                    public void run() {
                        new AppDatabase(SettingsActivity.this, null).getHistory().clear();
                    }
                });

        findPreference("clearAllNamespaces").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle args = new Bundle();
                args.putInt("id", DIALOG_CLEAR_NAMESPACES);
                args.putInt("title", R.string.pref_clearAllNamespaces_title);
                args.putInt("message", R.string.pref_clearAllNamespaces_message);
                args.putInt("positiveBtn", R.string.yes);
                args.putInt("negativeBtn", R.string.no);
                IfDialogFragment dialog = new IfDialogFragment();
                dialog.setArguments(args);
                getFragmentManager().beginTransaction()
                        .add(dialog, "dialog")
                        .commit();
                return true;
            }
        });

        findPreference("separateNamespace").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mResultIntent.putExtra("updateSelectedKit", true);
                return true;
            }
        });

        findPreference("editNamespace").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, NamespaceActivity.class);
                intent.putExtra("kitNames", getIntent().getStringArrayExtra("kitNames"));
                // This is how kitNames should NOT be passed to NamespaceActivity
                startActivityForResult(intent, REQUEST_EDIT_NAMESPACE);
                return true;
            }
        });

        findPreference("editKeyboardKits").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, KeyboardsActivity.class);
                startActivityForResult(intent, REQUEST_EDIT_KEYBOARD_KITS);
                return true;
            }
        });

        findPreference("darkOrangeEquals").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                onThemeValueChanged();
                return true;
            }
        });
    }

    private boolean themeChangedNotificationShown = false;
    private void onThemeValueChanged() {
        if(themeChangedNotificationShown) return;
        themeChangedNotificationShown = true;
        Toast.makeText(SettingsActivity.this, R.string.pref_theme_valueChanged, Toast.LENGTH_SHORT)
                .show();
        mResultIntent.putExtra("themeChanged", true);
    }

    private void onInputDigitGroupingChanged() {
        mResultIntent.putExtra("inputDigitGroupingChanged", true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_EDIT_NAMESPACE) {
            mResultIntent.putExtra("updateSelectedKit", data.getBooleanExtra("selectedKitChanged", false));
        } else if(requestCode == REQUEST_EDIT_KEYBOARD_KITS) {
            mResultIntent.putExtra("updateKitViews", resultCode == Activity.RESULT_OK);
        }
    }

    @Override
    public void onDialogPositiveClick(int dialogId) {
        if(dialogId == DIALOG_CLEAR_NAMESPACES) {
            Preference pref = findPreference("clearAllNamespaces");
            pref.setEnabled(false);
            pref.setSummary(R.string.pref_clearAllNamespaces_summary_empty);
            mResultIntent.putExtra("clearAllNamespaces", true);
        }
    }

    @Override public void onDialogNegativeClick(int dialogId) {}
    @Override public void onDialogNeutralClick(int dialogId) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /* *
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     * /
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                //|| DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                //|| NotificationPreferenceFragment.class.getName().equals(fragmentName)
                ;
    }

    /* *
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * /
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * /
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }*/

    /* *
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     * /
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }*/
}
