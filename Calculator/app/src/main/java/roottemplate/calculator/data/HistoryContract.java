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

package roottemplate.calculator.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import roottemplate.calculator.util.Util;

public class HistoryContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                    HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    HistoryEntry.COLUMN_NAME_LEFT + " TEXT," +
                    HistoryEntry.COLUMN_NAME_RIGHT + " TEXT," +
                    HistoryEntry.COLUMN_NAME_ERROR + " TEXT," +
                    HistoryEntry.COLUMN_NAME_TIME + " TIMESTAMP DEFAULT 0" +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME;



    private final AppDatabase mDb;

    public HistoryContract(AppDatabase db) {
        this.mDb = db;
    }

    public void clear() {
        new ClearHistoryTask().execute(mDb);
    }

    public int getElementCount() {
        Cursor c = mDb.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + HistoryEntry.TABLE_NAME, null);
        c.moveToFirst();
        int result = c.getInt(0);
        c.close();
        return result;
    }

    public void addHistoryElement(String left, String right, String error) {
        new AddHistoryElementTask().execute(this, left, right, error);
    }

    public Cursor getHistoryElements() {
        return mDb.getReadableDatabase().query(
                HistoryEntry.TABLE_NAME,
                new String[]{
                        HistoryEntry._ID,
                        HistoryEntry.COLUMN_NAME_LEFT,
                        HistoryEntry.COLUMN_NAME_RIGHT,
                        HistoryEntry.COLUMN_NAME_ERROR
                },
                null,
                null,
                null,
                null,
                HistoryEntry._ID + " DESC");
    }


    public void updateDatabase(boolean isAppClosing, boolean doRunInBackground) {
        if(doRunInBackground) {
            new UpdateHistoryTask().execute(this, isAppClosing);
            return;
        }

        int storingHistory = mDb.getPrefs().storingHistory();
        if(storingHistory == 0 && !isAppClosing) return;

        String clause;
        String[] args;
        if(storingHistory == 0) {
            clause = null;
            args = null;
        } else if(storingHistory < 0) { // by time
            clause = HistoryEntry.COLUMN_NAME_TIME + " < ?";
            args = new String[] {(System.currentTimeMillis() / 1000 + storingHistory * 60) + ""};
            // Remember: storingHistory < 0!
        } else { // by count
            Cursor maxC = mDb.getReadableDatabase().rawQuery("SELECT MAX(" + HistoryEntry._ID + ") FROM " +
                    HistoryEntry.TABLE_NAME, null);
            maxC.moveToFirst();
            int max = maxC.getInt(0);
            maxC.close();

            clause = HistoryEntry._ID + " < ?";
            args = new String[] {(max - storingHistory) + ""};
        }

        mDb.getWritableDatabase().delete(HistoryEntry.TABLE_NAME, clause, args);
    }



    public static final class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_LEFT = "left";
        public static final String COLUMN_NAME_RIGHT = "right";
        public static final String COLUMN_NAME_ERROR = "error";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static class AddHistoryElementTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            HistoryContract db_ = (HistoryContract) params[0];
            //int storingHistory = db_.mDb.getPrefs().storingHistory();
            String left = (String) params[1];
            String right = params.length > 2 ? (String) params[2] : null;
            String error = params.length > 3 ? (String) params[3] : null;

            SQLiteDatabase db = db_.mDb.getWritableDatabase();
            Cursor lastEntry = db.query(HistoryEntry.TABLE_NAME,
                    new String[]{
                            HistoryEntry.COLUMN_NAME_LEFT,
                            HistoryEntry.COLUMN_NAME_RIGHT,
                            HistoryEntry.COLUMN_NAME_ERROR
                    },
                    null, null, null, null, HistoryEntry._ID + " DESC", "1");
            boolean repeating = lastEntry.moveToFirst() &&
                    lastEntry.getString(lastEntry.getColumnIndex(HistoryEntry.COLUMN_NAME_LEFT)).equals(left) &&
                    Util.equals(lastEntry.getString(lastEntry.getColumnIndex(HistoryEntry.COLUMN_NAME_RIGHT)), right) &&
                    Util.equals(lastEntry.getString(lastEntry.getColumnIndex(HistoryEntry.COLUMN_NAME_ERROR)), error);
            lastEntry.close();
            if(repeating) return null;

            ContentValues values = new ContentValues();
            values.put(HistoryEntry.COLUMN_NAME_LEFT, left);
            if(right != null)
                values.put(HistoryEntry.COLUMN_NAME_RIGHT, right);
            else
                values.putNull(HistoryEntry.COLUMN_NAME_RIGHT);
            if(error != null)
                values.put(HistoryEntry.COLUMN_NAME_ERROR, error);
            else
                values.putNull(HistoryEntry.COLUMN_NAME_ERROR);
            //if(storingHistory < 0)
                values.put(HistoryEntry.COLUMN_NAME_TIME, System.currentTimeMillis() / 1000);

            long row = db.insert(HistoryEntry.TABLE_NAME, null, values);
            if(row % 20 == 0)
                db_.updateDatabase(false, false);
            return null;
        }
    }

    public static class UpdateHistoryTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            ((HistoryContract) params[0]).updateDatabase((Boolean) params[1], false);
            return null;
        }
    }

    public static class ClearHistoryTask extends AsyncTask<AppDatabase, Void, Void> {
        @Override
        protected Void doInBackground(AppDatabase... params) {
            params[0].getWritableDatabase().delete(HistoryEntry.TABLE_NAME, null, null);
            return null;
        }
    }
}
