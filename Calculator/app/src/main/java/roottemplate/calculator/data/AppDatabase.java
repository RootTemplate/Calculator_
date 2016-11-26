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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import roottemplate.calculator.PreferencesManager;

public class AppDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "data.db";


    private final PreferencesManager mPrefs;
    private final HistoryContract mHistory;
    private final NamespaceContract mNamespace;

    public AppDatabase(Context context, PreferencesManager prefs) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mPrefs = prefs;
        mHistory = new HistoryContract(this);
        mNamespace = new NamespaceContract(this);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryContract.SQL_CREATE_ENTRIES);
        db.execSQL(NamespaceContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(HistoryContract.SQL_DELETE_ENTRIES);
        db.execSQL(NamespaceContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public PreferencesManager getPrefs() {
        return mPrefs;
    }

    public HistoryContract getHistory() {
        return mHistory;
    }

    public NamespaceContract getNamespace() {
        return mNamespace;
    }
}
