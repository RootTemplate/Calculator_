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

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import roottemplate.calculator.data.AppDatabase;
import roottemplate.calculator.data.HistoryContract;
import roottemplate.calculator.util.Util;

public class HistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private HistoryContract mHistory;
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setResult(RESULT_CANCELED);
        mHistory = new AppDatabase(this, new PreferencesManager(this)).getHistory();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
            newConfig.uiMode = (newConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_YES;

        super.onConfigurationChanged(newConfig);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new HistoryLoader(this, mHistory);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() > 0) {
            ListView list = (ListView) findViewById(R.id.historyElems);
            list.setVisibility(View.VISIBLE);

            mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
                    new String[] {
                            HistoryContract.HistoryEntry.COLUMN_NAME_LEFT
                    }, new int[] {
                            android.R.id.text1
                    }
            );

            final String colorMain = Util.intToHexColor(getResources().getColor(R.color.colorInputText));
            final String colorEquals = Util.intToHexColor(getResources().getColor(R.color.colorEquals));
            final String colorError = Util.intToHexColor(getResources().getColor(R.color.colorErrorDetail));
            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<font color=\"").append(colorMain).append("\">")
                            .append(cursor.getString(columnIndex));

                    int rightColumn = cursor.getColumnIndex(HistoryContract.HistoryEntry.COLUMN_NAME_RIGHT);
                    boolean rightNull = cursor.isNull(rightColumn);
                    if(!rightNull)
                        sb.append(" = ");
                    sb.append("</font>");
                    if(!rightNull) {
                        String right = cursor.getString(rightColumn);
                        sb.append("<font color=\"").append(colorEquals).append("\">").append(right).append("</font>");
                    }

                    int errorColumn = cursor.getColumnIndex(HistoryContract.HistoryEntry.COLUMN_NAME_ERROR);
                    if(!cursor.isNull(errorColumn))
                        sb.append(" <font color=\"").append(colorError).append("\">(")
                                .append(cursor.getString(errorColumn)).append(")</font>");

                    ((TextView) view).setText(Html.fromHtml(sb.toString()));
                    return true;
                }
            });
            list.setAdapter(mAdapter);

            registerForContextMenu(list);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    view.showContextMenu();
                }
            });
        } else {
            findViewById(R.id.historyEmpty).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String[] getHistoryElems(ContextMenu.ContextMenuInfo menuInfo) {
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        int rightColumn = cursor.getColumnIndex(HistoryContract.HistoryEntry.COLUMN_NAME_RIGHT);
        int errorColumn = cursor.getColumnIndex(HistoryContract.HistoryEntry.COLUMN_NAME_ERROR);

        return new String[] {
                cursor.getString(cursor.getColumnIndex(HistoryContract.HistoryEntry.COLUMN_NAME_LEFT)),
                cursor.isNull(rightColumn) ? null : cursor.getString(rightColumn),
                cursor.isNull(errorColumn) ? null : cursor.getString(errorColumn)
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history_context, menu);

        boolean hasResult = getHistoryElems(menuInfo)[1] != null;
        if(!hasResult) {
            for(int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                int id = item.getItemId();
                if(id == R.id.menuItem_history_copyResult ||
                        id == R.id.menuItem_history_pasteResult)
                    item.setVisible(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String[] elems = getHistoryElems(item.getMenuInfo());
        String paste = null;
        switch (item.getItemId()) {
            case R.id.menuItem_history_copyExpr:
                Util.setPrimaryClip(this, elems[0]);
                return true;
            case R.id.menuItem_history_copyResult:
                Util.setPrimaryClip(this, elems[1]);
                return true;
            case R.id.menuItem_history_copyAll:
                StringBuilder sb = new StringBuilder(elems[0]);
                if(elems[1] != null)
                    sb.append(" = ").append(elems[1]);
                if(elems[2] != null)
                    sb.append(" (").append(elems[2]).append(")");
                Util.setPrimaryClip(this, sb.toString());
                return true;
            case R.id.menuItem_history_pasteExpr:
                paste = elems[0];
                break;
            case R.id.menuItem_history_pasteResult:
                paste = elems[1];
                break;
        }

        if(paste != null) {
            Intent intent = new Intent();
            intent.putExtra("paste", paste);
            setResult(RESULT_OK, intent);
            finish();
        }

        return super.onContextItemSelected(item);
    }



    private static class HistoryLoader extends CursorLoader {
        private final HistoryContract mDatabase;

        public HistoryLoader(Context context, HistoryContract database) {
            super(context);
            mDatabase = database;
        }

        @Override
        public Cursor loadInBackground() {
            mDatabase.updateDatabase(false, false);
            return mDatabase.getHistoryElements();
        }
    }
}
