package com.example.mynavigator.ui.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportAdapter {

    protected static final String TAG = "ReportAdapater";
    private static String DB_NAME_RPT ="data_all.db";
    // TODO : TABLE 이름을 명시해야함
    protected static final String TABLE_NAME = "report_table";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public ReportAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext, DB_NAME_RPT, 1);
    }

    public ReportAdapter createDatabase() throws SQLException
    {
        try
        {
            mDbHelper.createDataBase();
        }
        catch (IOException mIOException)
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public ReportAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase(DB_NAME_RPT);
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close()
    {
        mDbHelper.close();
    }

    public List getTableData()
    {
        try
        {
            String sql ="SELECT * FROM " + TABLE_NAME;

            // 모델 넣을 리스트 생성
            List placeList = new ArrayList();

            // TODO : 모델 선언
            RptData reportdata = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    reportdata = new RptData();

                    // TODO : Record 기술
                    // id, name, account, privateKey, secretKey, Comment
                    reportdata.setSenderName(mCur.getString(0));
                    reportdata.setAccidentType(mCur.getString(1));
                    reportdata.setLatitude(mCur.getFloat(2));
                    reportdata.setLongitude(mCur.getFloat(3));
                    reportdata.setReasonSelected(mCur.getString(4));


                    // 리스트에 넣기
                    placeList.add(reportdata);
                }

            }
            return placeList;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }
}
