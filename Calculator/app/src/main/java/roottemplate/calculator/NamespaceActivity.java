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
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import roottemplate.calculator.data.AppDatabase;
import roottemplate.calculator.data.NamespaceContract;
import roottemplate.calculator.util.Util;

public class NamespaceActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private AppDatabase mDb;
    private String[] mKitNames;
    private ListView mNamespaceContent;
    private SimpleCursorAdapter mListAdapter;
    private String mCurrentKitName;
    private String mEvaluatorSelectedKitName; // The name of kit loaded in Evaluator in MainActivity's fragment
    private Intent mResultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_namespace);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mDb = new AppDatabase(this, new PreferencesManager(this));
        mNamespaceContent = (ListView) findViewById(R.id.namespaceContent);
        mResultIntent = new Intent();
        setResult(RESULT_OK, mResultIntent);

        String[] allKitNames = getIntent().getStringArrayExtra("kitNames");
        mKitNames = new String[allKitNames.length / 2 + 1];
        String[] spinnerKitNames = new String[mKitNames.length];
        mEvaluatorSelectedKitName = mDb.getPrefs().separateNamespace() ? mDb.getPrefs().kitName() : null;
        int selectedKitIndex = 0;
        for(int i = 0; i < allKitNames.length / 2; i++) {
            mKitNames[i + 1] = allKitNames[i * 2]; // TODO: check if shortName exists
            spinnerKitNames[i + 1] = allKitNames[i * 2] + " (" + allKitNames[i * 2 + 1] + ")";
            if(mKitNames[i + 1].equals(mEvaluatorSelectedKitName))
                selectedKitIndex = i + 1;
        }
        spinnerKitNames[0] = getResources().getString(R.string.namespace_common_ns);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerKitNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentKitName = mKitNames[position];
                initLoader();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.clearNamespace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDb.getNamespace().clearNamespace(mCurrentKitName);
                mListAdapter.changeCursor(null);
                setSelectedEvaluatorChanged();
            }
        });

        spinner.setSelection(selectedKitIndex);

        initListAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSelectedEvaluatorChanged() {
        mResultIntent.putExtra("selectedKitChanged", true);
    }

    private void initListAdapter() {
        mListAdapter = new SimpleCursorAdapter(
                this,
                R.layout.namespace_list_item,
                null,
                new String[] {
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_NAME,
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_IS_STANDARD,
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_IS_STANDARD // Hack
                },
                new int[] { R.id.text, R.id.isStandard, R.id.delete },
                0
        );
        mListAdapter.setViewBinder(new ListViewBinder());
        mNamespaceContent.setAdapter(mListAdapter);
    }


    private void initLoader() {
        //mListAdapter.changeCursor(null);
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new NamespaceLoader(this, mDb, mCurrentKitName);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}


    public void onSetStandardClick(View view) {
        int id = (int) ((View) view.getParent()).getTag();
        mDb.getNamespace().setStandard(id, ((CheckBox) view).isChecked());
    }

    public void onDeleteClick(View view) {
        int id = (int) ((View) view.getParent()).getTag();
        mDb.getNamespace().removeNamespaceElement(id, false); // Running in UI Thread
        initLoader();

        if(Util.equals(mCurrentKitName, mEvaluatorSelectedKitName))
            setSelectedEvaluatorChanged();
    }


    private static class NamespaceLoader extends CursorLoader {
        private final AppDatabase mDb;
        private final String mKitName;

        public NamespaceLoader(Context context, AppDatabase db, String kitName) {
            super(context);
            mDb = db;
            mKitName = kitName;
        }

        @Override
        public Cursor loadInBackground() {
            mDb.getNamespace().updateDatabase(false, false);
            return mDb.getNamespace().getNamespace(mKitName);
        }
    }

    private class ListViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()) {
            case R.id.text:
                // Set ID of the namespace element as tag of parent-LinearLayout to identify
                // the View later
                ((View) view.getParent()).setTag(cursor.getInt(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry._ID)));

                int type = cursor.getInt(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_TYPE));
                String name = cursor.getString(columnIndex);
                String expr = cursor.getString(cursor.getColumnIndex(
                        NamespaceContract.NamespaceEntry.COLUMN_NAME_EXPRESSION));
                SpannableStringBuilder ssb = new SpannableStringBuilder();

                int start = 0;
                if(type == 0) {
                    // NUMBER
                    ssb.append(name).append(" = ");
                    start = ssb.length();
                    ssb.append(expr);
                } else if(type == 1) {
                    String[] func = expr.split(";");
                    ssb.append(name).append("(");
                    for(int i = 1; i < func.length; i++) {
                        if(i != 1) ssb.append(", ");
                        ssb.append(func[i]);
                    }
                    ssb.append(") = ");
                    start = ssb.length();
                    ssb.append(func[0]);
                } else
                    Log.e(Util.LOG_TAG, "[NamespaceActivity] Found new type but no support added: "
                            + type);

                ssb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorNamespaceEquals)),
                        start, ssb.length(), 0);

                ((TextView) view).setText(ssb);
                break;
            case R.id.isStandard:
                ((CheckBox) view).setChecked(cursor.getInt(columnIndex) == 1);
                break;
            case R.id.delete:
                ((ImageButton) view).setColorFilter(getResources().getColor(R.color.colorButtonShiftText),
                        PorterDuff.Mode.MULTIPLY);
                break;
            }
            return true;
        }
    }
}
