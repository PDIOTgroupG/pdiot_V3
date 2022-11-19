package com.specknet.pdiotapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.specknet.pdiotapp.bean.HistoryData;
import com.specknet.pdiotapp.bean.User;

import java.util.Objects;

public class MySQLite extends SQLiteOpenHelper {
    private static final String DB_NAME = "mySQLite.db";
    private static final String TABLE_NAME_USER = "user";
    private static final String TABLE_NAME_HISTORY_DATA = "history";

    private static final String CREATE_USER_TABLE_SQL = "create table IF NOT EXISTS "+TABLE_NAME_USER+"(id integer primary key autoincrement,name text,account text,password text);";
    private static final String CREATE_HISTORY_TABLE_SQL = "create table IF NOT EXISTS "+TABLE_NAME_HISTORY_DATA+"(id integer primary key autoincrement,name text,activity text,date text);";


    public MySQLite(Context context){
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE_SQL);
        db.execSQL(CREATE_HISTORY_TABLE_SQL);
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
            return false;
        }

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


    public long insertHistory(HistoryData historyData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",historyData.getName());
        values.put("activity",historyData.getActivity());
        values.put("date",historyData.getDate());
        return db.insert(TABLE_NAME_HISTORY_DATA,null,values);
    }


    public int test(String date,String name){
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from history where date=? and name=?",new String[]{date,name});
            return c.getCount();
        }catch (SQLException e){
            return 0;
        }

    }

    public int test1(String date,String name,String activity){
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from history where date=? and name=? and activity=?",new String[]{date,name,activity});
            return c.getCount();
        }catch (SQLException e){
            return 0;
        }

    }


    public boolean existsDate(String date) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from history where date=?",new String[]{date});
            if (c.getCount()!=0){
                return true;
            }else{
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public int getWalkingCount(String date,String name){
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from history where date=? and name=? and activity=?",new String[]{date,name,"Walking"});
            return c.getCount();
        }catch (SQLException e){
            return 0;
        }
    }

    public int getRunningCount(String date,String name){
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from history where date=? and name=? and activity=?",new String[]{date,name,"Running"});
            return c.getCount();
        }catch (SQLException e){
            return 0;
        }

    }





    }
