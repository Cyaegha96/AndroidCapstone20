package com.mocca.moccaCanary.menu.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;

public class CommentDataAdapter {

    protected static final String TAG = "CommentDataAdapter";
    private static String DB_NAME_TMACS= "tmacs_data.db";
    // TODO : TABLE 이름을 명시해야함


    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public CommentDataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext, DB_NAME_TMACS, 1);
    }

    public CommentDataAdapter createDatabase() throws SQLException
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

    public CommentDataAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase(DB_NAME_TMACS);
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

    public CommentData getTableData(String Region,int index)
    {
        try
        {
            String sql = "SELECT * FROM " + Region +"_코멘트 " + "WHERE " +"인덱스 ="+index;
            Log.d(TAG,"SELECT * FROM " + Region +"_코멘트 " + "WHERE " +"인덱스 = "+index);
            // 모델 넣을 리스트 생성

            // TODO : 모델 선언
           CommentData commentData=null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                // 칼럼의 마지막까지
                while (mCur.moveToNext()) {

                    // TODO : 커스텀 모델 생성
                    commentData = new CommentData();

                    // TODO : Record 기술
                    commentData.setIndex(mCur.getInt(0));
                    commentData.setComment(mCur.getString(3));
                }
            }
            return commentData;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

}
