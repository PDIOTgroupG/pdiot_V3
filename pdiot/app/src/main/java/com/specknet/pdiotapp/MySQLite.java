package com.specknet.pdiotapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.specknet.pdiotapp.bean.User;

import java.util.Objects;

public class MySQLite extends SQLiteOpenHelper {
    private static final String DB_NAME = "mySQLite.db";
    private static final String TABLE_NAME_USER = "user";
    private static final String TABLE_NAME_HISTORY_DATA = "history";

    private static final String CREATE_USER_TABLE_SQL = "create table "+TABLE_NAME_USER+"(id integer primary key autoincrement,name text,account text,password text);";

    public MySQLite(Context context){
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public long insertRegister(User user){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",user.getName());
        values.put("account",user.getUserID());
        values.put("password",user.getPassword());
        return db.insert(TABLE_NAME_USER,null,values);
    }

    public boolean checkNoSuchAccount(String account){
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from user where account=?",new String[]{account});
        if(c.moveToFirst()) {
            String password = c.getString(c.getColumnIndex("password"));
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    public String checkPwd(String account){
        SQLiteDatabase db = getWritableDatabase();
//        Cursor cursor = db.query(TABLE_NAME_USER,null,"account like ?",new String[]{account},null,null,
//                null);
        Cursor c = db.rawQuery("select * from user where account=?",new String[]{account});
        if(c.moveToFirst()) {
            String password = c.getString(c.getColumnIndex("password"));
            return password;
        }

        return null;
    }

    public String NameOfAccount(String account){
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from user where account=?",new String[]{account});
        if(c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex("name"));
            return name;
        }
        return "GUEST";
    }



}
