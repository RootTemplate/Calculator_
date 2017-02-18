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

public class NamespaceContract {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NamespaceEntry.TABLE_NAME + " (" +
                    NamespaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NamespaceEntry.COLUMN_NAME_KIT + " TEXT," +
                    NamespaceEntry.COLUMN_NAME_NAME + " TEXT," +
                    NamespaceEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    NamespaceEntry.COLUMN_NAME_EXPRESSION + " TEXT," +
                    NamespaceEntry.COLUMN_NAME_IS_STANDARD + " INTEGER," +
                    NamespaceEntry.COLUMN_NAME_TIME + " TIMESTAMP DEFAULT 0" +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NamespaceEntry.TABLE_NAME;


    private final AppDatabase mDb;

    public NamespaceContract(AppDatabase db) {
        mDb = db;
    }

    public void clearNamespace(String kit) {
        new ClearNamespaceTask().execute(mDb, kit);
    }

    public void clearAllNamespace() {
        new ClearAllNamespaceTask().execute(mDb);
    }

    public void putNamespaceElement(String kit, String name, int type, String expr,
                                    boolean isStandard) {
        new PutNamespaceElementTask().execute(mDb, kit, name, type, expr, isStandard);
    }

    public Cursor getNamespace(String kit) {
        return mDb.getReadableDatabase().query(
                NamespaceEntry.TABLE_NAME,
                new String[] {
                        NamespaceEntry._ID,
                        NamespaceEntry.COLUMN_NAME_NAME,
                        NamespaceEntry.COLUMN_NAME_TYPE,
                        NamespaceEntry.COLUMN_NAME_EXPRESSION,
                        NamespaceEntry.COLUMN_NAME_IS_STANDARD
                },
                NamespaceEntry.COLUMN_NAME_KIT + (kit == null ? " is NULL" : " = ?"),
                (kit == null) ? null : new String[] {kit},
                null,
                null,
                NamespaceEntry._ID + " ASC");
    }

    public void updateDatabase(boolean isAppClosing, boolean runInBackground) {
        if(runInBackground) {
            new UpdateNamespaceTask().execute(this, isAppClosing);
            return;
        }

        int storing = mDb.getPrefs().storingNamespace();
        if(storing == 0 && !isAppClosing || storing == -1) return;

        String clause;
        String[] args;
        if(storing == 0) {
            clause = NamespaceEntry.COLUMN_NAME_IS_STANDARD + " != 1";
            args = null;
        } else { // by time
            clause = NamespaceEntry.COLUMN_NAME_TIME + " < ? AND " +
                    NamespaceEntry.COLUMN_NAME_IS_STANDARD + " != 1";
            args = new String[] {String.valueOf(System.currentTimeMillis() / 1000 / 60 / 60 - storing * 24)};
        }

        mDb.getWritableDatabase().delete(NamespaceEntry.TABLE_NAME, clause, args);
    }

    public void removeNamespaceElement(int id, boolean runInBackground) {
        if(runInBackground) {
            new RemoveNamespaceElementTask().execute(this, id);
            return;
        }

        mDb.getWritableDatabase().delete(
                NamespaceEntry.TABLE_NAME,
                NamespaceEntry._ID + " = ?",
                new String[] { String.valueOf(id) }
        );
    }

    public void setStandard(int id, boolean isStandard) {
        new SetStandardTask().execute(mDb, id, isStandard);
    }


    public static final class NamespaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "namespace";
        public static final String COLUMN_NAME_KIT = "kit";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_EXPRESSION = "expr";
        public static final String COLUMN_NAME_IS_STANDARD = "isStandard";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static class PutNamespaceElementTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            AppDatabase db_ = (AppDatabase) params[0];
            //int storingNs = db_.getPrefs().storingNamespace();
            String kit = (String) params[1], name = (String) params[2];
            int type = (int) params[3];

            SQLiteDatabase db = db_.getWritableDatabase();
            ContentValues values = new ContentValues();
            if(kit != null)
                values.put(NamespaceEntry.COLUMN_NAME_KIT, kit);
            else
                values.putNull(NamespaceEntry.COLUMN_NAME_KIT);
            values.put(NamespaceEntry.COLUMN_NAME_NAME, name);
            values.put(NamespaceEntry.COLUMN_NAME_TYPE, type);
            values.put(NamespaceEntry.COLUMN_NAME_EXPRESSION, (String) params[4]);
            values.put(NamespaceEntry.COLUMN_NAME_IS_STANDARD, (Boolean) params[5]);
            //if(storingNs > 0)
                values.put(NamespaceEntry.COLUMN_NAME_TIME, System.currentTimeMillis() / 1000 / 60 / 60);

            String[] selectionArgs = kit == null ?
                    new String[] {name, String.valueOf(type)} :
                    new String[] {kit, name, String.valueOf(type)};
            Cursor c = db.query(
                    NamespaceEntry.TABLE_NAME,
                    new String[] {NamespaceEntry._ID},
                    NamespaceEntry.COLUMN_NAME_KIT + (kit == null ? " is NULL" : " = ?") + " AND " +
                            NamespaceEntry.COLUMN_NAME_NAME + " = ? AND " +
                            NamespaceEntry.COLUMN_NAME_TYPE + " = ?",
                    selectionArgs,
                    null,
                    null,
                    NamespaceEntry._ID + " ASC",
                    "1"
            );
            if(!c.moveToFirst())
                db.insert(NamespaceEntry.TABLE_NAME, null, values);
            else {
                db.update(
                        NamespaceEntry.TABLE_NAME,
                        values,
                        NamespaceEntry._ID + " = ?",
                        new String[] {String.valueOf(c.getInt(c.getColumnIndex(NamespaceEntry._ID)))}
                );
            }
            c.close();
            return null;
        }
    }

    public static class UpdateNamespaceTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            ((NamespaceContract) params[0]).updateDatabase((Boolean) params[1], false);
            return null;
        }
    }

    public static class ClearNamespaceTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            AppDatabase db = (AppDatabase) params[0];
            String kit = (String) params[1];
            db.getWritableDatabase().delete(
                    NamespaceEntry.TABLE_NAME,
                    NamespaceEntry.COLUMN_NAME_KIT + (kit == null ? " is NULL" : " = ?"),
                    kit == null ? null : new String[] { (String) params[1] }
            );
            return null;
        }
    }

    public static class ClearAllNamespaceTask extends AsyncTask<AppDatabase, Void, Void> {
        @Override
        protected Void doInBackground(AppDatabase... params) {
            params[0].getWritableDatabase().delete(
                    NamespaceEntry.TABLE_NAME,
                    null,
                    null
            );
            return null;
        }
    }

    public static class RemoveNamespaceElementTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            ((NamespaceContract) params[0]).removeNamespaceElement((int) params[1], false);
            return null;
        }
    }

    public static class SetStandardTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            AppDatabase db = (AppDatabase) params[0];
            ContentValues values = new ContentValues();
            values.put(NamespaceEntry.COLUMN_NAME_IS_STANDARD, (boolean) params[2]);
            db.getWritableDatabase().update(
                    NamespaceEntry.TABLE_NAME,
                    values,
                    NamespaceEntry._ID + " = ?",
                    new String[] { String.valueOf((int) params[1]) }
            );
            return null;
        }
    }
}
