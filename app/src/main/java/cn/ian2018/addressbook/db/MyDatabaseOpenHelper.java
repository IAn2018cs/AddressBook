package cn.ian2018.addressbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/11/23/023.
 */

public class MyDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String CREATE_USER = "CREATE TABLE T_User(Id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " Name TEXT, Phone TEXT, QQ TEXT, Email TEXT, Address TEXT)";

    public MyDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
