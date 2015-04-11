/**
 * MySQLiteOpenHelper
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    static final String DB = "sqlite_tonguetwisterteacher.db";
    static final String TABLE_NAME = "sentences";
    static final int DB_VERSION = 1;
    static final String CREATE_TABLE = "create table " + TABLE_NAME  + " (" +
                                       "_id integer primary key autoincrement, " +
                                       "sentence text not null, " +
                                       "cntAll integer, " +
                                       "cntSuccess integer, " +
                                       "record real " +
                                       ")";
    static final String DROP_TABLE = "drop table mytable;";
    public MySQLiteOpenHelper(Context c) {
        super(c, DB, null, DB_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
