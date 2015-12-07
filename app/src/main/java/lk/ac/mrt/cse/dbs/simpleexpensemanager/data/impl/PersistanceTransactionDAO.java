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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.AppContext;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

/**
 * This is an In-Memory implementation of TransactionDAO interface. This is not a persistent storage. All the
 * transaction logs are stored in a LinkedList in memory.
 */
public class PersistanceTransactionDAO implements TransactionDAO {

    private DataBase dataBase;
    public static final String DATE ="date";
    public static final String EXPENCE_TYPE ="expense_type";
    public static final String AMOUNT ="amount";
    public static final String ACCOUNT_NO ="account_no";

    public PersistanceTransactionDAO(DataBase databaseE)
    {
        this.dataBase=databaseE;
    }
    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTime = sdf.format(date);
        try {

            SQLiteDatabase database = dataBase.openToWrite();
            ContentValues cv = new ContentValues();
            cv.put(PersistanceTransactionDAO.ACCOUNT_NO, accountNo);
            cv.put(PersistanceTransactionDAO.DATE, DateTime);
            cv.put(PersistanceTransactionDAO.EXPENCE_TYPE, expenseType.toString());
            cv.put(PersistanceTransactionDAO.AMOUNT, amount);
            database.insert("transactions", null, cv);
            database.close();
            Log.i("log transaction ok", accountNo+" "+DateTime+" "+expenseType.toString()+" "+amount);

        }
        catch(Exception ex){
           Log.i(" Error Transaction log", ex.getMessage());
        }
    }

    public Transaction getTransactionFromCursor(Cursor cursor)
    {
        String account_no="";
        String expense_type="";
        Date transactionDate=new Date();
        String DateTime="";

        double amount=0;
        try {

            if (cursor != null) {
                if (cursor.getColumnIndex(PersistanceTransactionDAO.ACCOUNT_NO) != -1) {
                    account_no = cursor.getString(cursor.getColumnIndexOrThrow(PersistanceTransactionDAO.ACCOUNT_NO));
                }
                 if (cursor.getColumnIndex(PersistanceTransactionDAO.DATE) != -1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                   // Log.i("SimpleDateFormat "," OK");
                    DateTime=cursor.getString(cursor.getColumnIndexOrThrow(PersistanceTransactionDAO.DATE));
                   // Log.i("DateTime",DateTime);
                    transactionDate = sdf.parse(DateTime);
                    //Log.i("Date",transactionDate.toString());
                }
                if (cursor.getColumnIndex(PersistanceTransactionDAO.EXPENCE_TYPE) != -1) {
                    expense_type = cursor.getString(cursor.getColumnIndexOrThrow(PersistanceTransactionDAO.EXPENCE_TYPE));
                }
                if (cursor.getColumnIndex(PersistanceTransactionDAO.AMOUNT) != -1) {
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(PersistanceTransactionDAO.AMOUNT));
                }
            }
            else
            {
                return null;
            }
        }
       catch(Exception ex)
            {
               Log.i("ERROR get transaction","from cursor date parse exception"+ex);
            }
        ExpenseType expenseType=ExpenseType.EXPENSE;
        switch (expense_type)
        {
            case "EXPENSE":
                expenseType=ExpenseType.EXPENSE;
                break;
            case "INCOME":
                expenseType=ExpenseType.INCOME;
                break;
        }
        Log.i("Transaction retrieved",transactionDate+" "+account_no+" "+expenseType.toString()+" "+amount );
        return new Transaction(transactionDate,account_no,expenseType,amount);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> transaction_list = new ArrayList<Transaction>();
        SQLiteDatabase database;
        try {
            database=dataBase.openToRead();
            Cursor cursor = database.rawQuery("select * from transactions",null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    transaction_list.add(getTransactionFromCursor(cursor));
                    cursor.moveToNext();
                }
                cursor.close();
            }
            database.close();
            return transaction_list;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> transaction_list =getAllTransactionLogs();
        if(transaction_list.size()<=limit){
            return transaction_list;
        }
        else
        {
            return transaction_list.subList(0,limit);
        }
    }
}
