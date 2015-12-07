/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.AppContext;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * This is an In-Memory implementation of the AccountDAO interface. This is not a persistent storage. A HashMap is
 * used to store the account details temporarily in the memory.
 */
public class PersistanceAccountDAO implements AccountDAO {

    private DataBase dataBase;
    public static final String ACCOUNT_NO ="account_no";
    public static final String BANK_NAME ="bank_name";
    public static final String ACCOUNT_HOLDER_NAME ="account_holder_name";
    public static final String BALANCE ="balance";

    public PersistanceAccountDAO(DataBase databaseE)
    {
       this.dataBase= databaseE;
    }

    @Override
    public List<String> getAccountNumbersList() {

        List<String> account_no_list = new ArrayList<String>();
        SQLiteDatabase database;
        try {
            database = dataBase.openToRead();
            Cursor cursor = database.rawQuery("select account_no from account",null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    if (cursor.getColumnIndex(PersistanceAccountDAO.ACCOUNT_NO) != -1) {
                        account_no_list.add(cursor.getString(cursor.getColumnIndexOrThrow(PersistanceAccountDAO.ACCOUNT_NO)));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
            database.close();
        }
        catch (Exception ex) {
            Log.i("Error","Hello " + ex.getMessage());
        }
        return account_no_list;
    }

    public Account getAccountFromCursor(Cursor cursor)
    {

        String account_no="";
        String bank_name="";
        String account_holder_name="";
        double balance=0;
        Log.i("get accountfromcursor", " cursor " +cursor.getCount()+"  cursor column count "+cursor.getColumnCount());
        try {
            if (cursor!=null) {
                Log.i("get accountfromcursor "," cursor Column index of account no"+ cursor.getColumnIndex(PersistanceAccountDAO.ACCOUNT_NO)+" ");
                Log.i("get accountfromcursor "," cursor Column index of bank_name"+ cursor.getColumnIndex(PersistanceAccountDAO.BANK_NAME)+" ");
                Log.i("get accountfromcursor "," cursor Column index of account holder"+ cursor.getColumnIndex(PersistanceAccountDAO.ACCOUNT_HOLDER_NAME)+" ");
                Log.i("get accountfromcursor "," cursor Column index of balance"+ cursor.getColumnIndex(PersistanceAccountDAO.BALANCE)+" ");

                ///////////////---------------------------------cursor should get back
                cursor.moveToFirst();
                account_no = cursor.getString(cursor.getColumnIndexOrThrow(PersistanceAccountDAO.ACCOUNT_NO));
                Log.i("get accountfromcursor", " check 1");
                bank_name = cursor.getString(cursor.getColumnIndex(PersistanceAccountDAO.BANK_NAME));
                Log.i("get accountfromcursor", " check 2");
                account_holder_name = cursor.getString(cursor.getColumnIndex(PersistanceAccountDAO.ACCOUNT_HOLDER_NAME));
                Log.i("get accountfromcursor", " check 3");
                balance = cursor.getDouble(cursor.getColumnIndex(PersistanceAccountDAO.BALANCE));
                Log.i("get accountfromcursor", " check 4");
            }
            else{
                Log.i("account from cursor", " null account retrieved from cursor");
                return null;
            }
        }
        catch(Exception ex)
        {
            Log.i("getaccountfromcursor","exception "+ ex.toString() );
        }

        Log.i("getAccountFromCurser"," account retrieved "+account_no+" "+bank_name+" "+account_holder_name+" "+balance);
        return new Account(account_no,bank_name,account_holder_name,balance);
    }

    @Override
    public List<Account> getAccountsList() {

        List<Account> account_list = new ArrayList<Account>();
        SQLiteDatabase database;
        try {
            database=dataBase.openToRead();
            Cursor cursor = database.rawQuery("select * from account", null);
            Log.i("getAccountList", "number of accounts = cursor count = "+cursor.getCount());
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    account_list.add(getAccountFromCursor(cursor));
                    cursor.moveToNext();
                }
                cursor.close();
            }
            database.close();

        }
        catch(Exception ex)
        {
         Log.i("GetAccountList Error",ex.toString());

        }
        return account_list;

    }
    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException
    {
        Cursor cursor =null ;
        try{
            SQLiteDatabase database = dataBase.openToRead();
            cursor = database.rawQuery("select * from account where account_no = ?", new String[]{accountNo});
            Account account =getAccountFromCursor(cursor);
            Log.i("getAccount", " account taken from account no " + accountNo);
            database.close();
            return account;
        }
        catch(Exception ex)
        {
            Log.i("get Account exception",ex.toString());
            return null;
        }


    }
    @Override
    public void addAccount(Account account) {
        SQLiteDatabase database;
        if(getAccountNumbersList().contains(account.getAccountNo()))
        {
            Log.i("addAccount"," account number "+account.getAccountNo()+" is already there");
            return;
        }

        try {
            database = dataBase.openToWrite();
            ContentValues cv = new ContentValues();
            cv.put(PersistanceAccountDAO.ACCOUNT_NO, account.getAccountNo());
            cv.put(PersistanceAccountDAO.BANK_NAME, account.getBankName());
            cv.put(PersistanceAccountDAO.ACCOUNT_HOLDER_NAME, account.getAccountHolderName());
            cv.put(PersistanceAccountDAO.BALANCE, account.getBalance());
            database.insert("account", null, cv);
            database.close();
            Log.i("add account", " account added " + account.getAccountNo() + " " + account.getBankName() + " "
                    + account.getAccountHolderName() + " " + account.getBalance());
        }
        catch (Exception ex)
        {
            Log.i("PersistanceAccountDAO", "Add account failed " + ex.toString());
        }
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        try {
            SQLiteDatabase database = dataBase.openToWrite();
            database.delete("account", "account_no = ?", new String[]{accountNo});
            database.close();
        }
        catch (Exception ex)
        {
           Log.i("remove account",ex.toString());
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        if(!getAccountNumbersList().contains(accountNo))
        {
            Log.i("update balance", "account number not found error");
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
        Account account = getAccount(accountNo);
        if(account==null)
        {
            Log.i("Account update","no account found");
        }

        // specific implementation based on the transaction type
        switch (expenseType) {
            case EXPENSE:
                account.setBalance(account.getBalance() - amount);
                break;
            case INCOME:
                account.setBalance(account.getBalance() + amount);
                break;
        }
        ContentValues cv =new ContentValues();
        cv.put(PersistanceAccountDAO.BALANCE, account.getBalance());

        try {
            SQLiteDatabase database = dataBase.openToWrite();
            database.update("account", cv, PersistanceAccountDAO.ACCOUNT_NO +"= ? ", new String[]{account.getAccountNo()});
            database.close();
        }
        catch(Exception ex)
        {
            Log.i("Update Balance ", "Exception" + ex.toString());
        }

    }
}
