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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;

public class HistoryDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "history.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryContract.TABLE_NAME + " (" +
                    HistoryContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    HistoryContract.COLUMN_NAME_LEFT + " TEXT," +
                    HistoryContract.COLUMN_NAME_RIGHT + " TEXT," +
                    HistoryContract.COLUMN_NAME_ERROR + " TEXT," +
                    HistoryContract.COLUMN_NAME_TIME + " TIMESTAMP DEFAULT 0" +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryContract.TABLE_NAME;



    private final PreferencesManager mPrefs;

    public HistoryDatabase(Context context, PreferencesManager prefs) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mPrefs = prefs;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void close() {
        getWritableDatabase().close();
    }

    public void clear() {
        new ClearHistoryTask().execute(this);
    }

    public int getElementCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + HistoryContract.TABLE_NAME, null);
        c.moveToFirst();
        int result =  c.getInt(0);
        c.close();
        return result;
    }

    public void addHistoryElement(String left, String right, String error) {
        new AddHistoryElementTask().execute(this, mPrefs.storingHistory(), left, right, error);
    }

    public Cursor getHistoryElements() {
        updateDatabase(false, false);
        return getReadableDatabase().query(
                HistoryContract.TABLE_NAME,
                new String[]{
                        HistoryContract._ID,
                        HistoryContract.COLUMN_NAME_LEFT,
                        HistoryContract.COLUMN_NAME_RIGHT,
                        HistoryContract.COLUMN_NAME_ERROR
                },
                null,
                null,
                null,
                null,
                HistoryContract._ID + " DESC");
    }


    public void updateDatabase(boolean isAppClosing, boolean doRunInBackground) {
        if(doRunInBackground) {
            new UpdateHistoryTask().execute(this, isAppClosing);
            return;
        }

        int storingHistory = mPrefs.storingHistory();
        if(storingHistory == 0 && !isAppClosing) return;

        String clause;
        String[] args;
        if(storingHistory == 0) {
            clause = null;
            args = null;
        } else if(storingHistory < 0) { // by time
            clause = HistoryContract.COLUMN_NAME_TIME + " < ?";
            args = new String[] {(System.currentTimeMillis() / 1000 + storingHistory * 60) + ""};
            // Remember: storingHistory < 0!
        } else { // by count
            Cursor maxC = getReadableDatabase().rawQuery("SELECT MAX(" + HistoryContract._ID + ") FROM " +
                    HistoryContract.TABLE_NAME, null);
            maxC.moveToFirst();
            int max = maxC.getInt(0);
            maxC.close();

            clause = HistoryContract._ID + " < ?";
            args = new String[] {(max - storingHistory) + ""};
        }

        getWritableDatabase().delete(HistoryContract.TABLE_NAME, clause, args);
        if(isAppClosing) close();
    }



    public static final class HistoryContract implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_LEFT = "left";
        public static final String COLUMN_NAME_RIGHT = "right";
        public static final String COLUMN_NAME_ERROR = "error";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static class AddHistoryElementTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            int storingHistory = (int) params[1];
            String left = (String) params[2];
            String right = params.length > 3 ? (String) params[3] : null;
            String error = params.length > 4 ? (String) params[4] : null;

            SQLiteDatabase db = ((HistoryDatabase) params[0]).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HistoryContract.COLUMN_NAME_LEFT, left);
            if(right != null)
                values.put(HistoryContract.COLUMN_NAME_RIGHT, right);
            else
                values.putNull(HistoryContract.COLUMN_NAME_RIGHT);
            if(error != null)
                values.put(HistoryContract.COLUMN_NAME_ERROR, error);
            else
                values.putNull(HistoryContract.COLUMN_NAME_ERROR);
            if(storingHistory < 0)
                values.put(HistoryContract.COLUMN_NAME_TIME, System.currentTimeMillis() / 1000);

            long row = db.insert(HistoryContract.TABLE_NAME, null, values);
            if(row % 20 == 0)
                ((HistoryDatabase) params[0]).updateDatabase(false, false);
            return null;
        }
    }

    public static class UpdateHistoryTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            ((HistoryDatabase) params[0]).updateDatabase((Boolean) params[1], false);
            return null;
        }
    }

    public static class ClearHistoryTask extends AsyncTask<HistoryDatabase, Void, Void> {
        @Override
        protected Void doInBackground(HistoryDatabase... params) {
            params[0].getWritableDatabase().delete(HistoryContract.TABLE_NAME, null, null);
            return null;
        }
    }
}
