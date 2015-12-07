package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.sql.SQLException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.*;

/**
 * Created by Pasindu Zee 7 on 04/12/2015.
 */
public class DataBase extends SQLiteOpenHelper{

    private static Context context;
    private static final String DATABASE_NAME = "my_database.db";
    private static int DATABASE_VERSION =1;
    private static final String TAG = "android_mini_project_database";
    SQLiteDatabase database;

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public SQLiteDatabase openToRead(){

        database = this.getReadableDatabase();
        return database;
    }

    public SQLiteDatabase openToWrite(){

        database = this.getWritableDatabase();
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DB", "DB created");
        String create_account_table_command = "create table account(" +
                "account_no text not null,"
                + "bank_name text not null,"
                + "account_holder_name text not null ,"
                + "balance double not null,"
                + "primary key(account_no),"
                +"check (balance>=0)"
                +");";

        //one transaction can only be happen at a time
        String create_transaction_table_command="create table transactions(" +
                "account_no text not null ,"
                +"date datetime not null,"
                +"expense_type text not null,"
                +"amount double not null," +
                "primary key(account_no,date),"+
                "check (amount>=0)"+
                ");";

        db.execSQL(create_account_table_command);
        db.execSQL(create_transaction_table_command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    }
