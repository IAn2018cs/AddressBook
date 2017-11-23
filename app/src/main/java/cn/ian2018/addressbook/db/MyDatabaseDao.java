package cn.ian2018.addressbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ian2018.addressbook.model.User;

/**
 * Created by Administrator on 2017/11/23/023.
 */

public class MyDatabaseDao {
    private static final String DB_NAME = "AddressBookDB";
    private static final int VERSION = 1;
    private final SQLiteDatabase db;
    private static MyDatabaseDao myDatabaseDao;
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private MyDatabaseDao(Context context) {
        MyDatabaseOpenHelper myDatabaseOpenHelper = new MyDatabaseOpenHelper(context, DB_NAME, null, VERSION);
        db = myDatabaseOpenHelper.getWritableDatabase();
    }

    public synchronized static MyDatabaseDao getInstance(Context context) {
        if (myDatabaseDao == null) {
            myDatabaseDao = new MyDatabaseDao(context);
        }
        return myDatabaseDao;
    }

    // 保存联系人
    public long saveUser(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", user.getName());
        contentValues.put("Phone", user.getPhone());
        contentValues.put("QQ", user.getQQ());
        contentValues.put("Email", user.getEmail());
        contentValues.put("Address", user.getAddress());

        long id = db.insert("T_User", null, contentValues);

        return id;
    }

    // 删除联系人
    public boolean deleteUser(int id) {
        boolean flag = false;
        int delete = db.delete("T_User", "Id = ?", new String[]{String.valueOf(id)});
        if (delete > 0) {
            flag = true;
        }
        return flag;
    }

    // 修改联系人
    public boolean updateUser(User user) {
        boolean flag = false;
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", user.getName());
        contentValues.put("Phone", user.getPhone());
        contentValues.put("QQ", user.getQQ());
        contentValues.put("Email", user.getEmail());
        contentValues.put("Address", user.getAddress());
        int update = db.update("T_User", contentValues, "Id = ?", new String[]{String.valueOf(user.getId())});
        if (update > 0) {
            flag = true;
        }
        return flag;
    }

    // 获取所有联系人
    public List<User> getUsers() {
        List<User> list = new ArrayList<>();
        Cursor cursor = db.query("T_User", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            User user = new User();
            user.setName(cursor.getString(cursor.getColumnIndex("Name")));
            user.setId(cursor.getInt(cursor.getColumnIndex("Id")));
            user.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
            user.setQQ(cursor.getString(cursor.getColumnIndex("QQ")));
            user.setEmail(cursor.getString(cursor.getColumnIndex("Email")));
            user.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
            list.add(user);
        }
        cursor.close();
        return list;
    }

    // 根据名字查询
    public List<User> getUsersForName(String name) {
        List<User> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM T_User WHERE Name LIKE ?", new String[]{"%"+name+"%"});
        while (cursor.moveToNext()) {
            User user = new User();
            user.setName(cursor.getString(cursor.getColumnIndex("Name")));
            user.setId(cursor.getInt(cursor.getColumnIndex("Id")));
            user.setPhone(cursor.getString(cursor.getColumnIndex("Phone")));
            user.setQQ(cursor.getString(cursor.getColumnIndex("QQ")));
            user.setEmail(cursor.getString(cursor.getColumnIndex("Email")));
            user.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
            list.add(user);
        }
        cursor.close();
        return list;
    }
}
