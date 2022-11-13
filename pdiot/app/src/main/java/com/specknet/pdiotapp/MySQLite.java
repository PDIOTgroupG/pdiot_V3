package com.specknet.pdiotapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.specknet.pdiotapp.bean.User;

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
        Cursor cursor = db.query(TABLE_NAME_USER,new String[]{"account"},"account like ?",new String[]{account},null,null,
                null);
        if (cursor != null){
            return false;
        }else {
            return true;
        }
    }

    public int checkPwd(String account,String pwd){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER,new String[]{"account"},"account like ?",new String[]{account},null,null,
                null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                String pwdInDB = cursor.getString(cursor.getColumnIndex("password"));
                if (pwd == pwdInDB){
                    return 1;
                }else {
                    return 0;
                }
            }

        }
        return -1;
    }



}
