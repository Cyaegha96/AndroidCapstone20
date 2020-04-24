package com.example.mynavigator.ui.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter
{
    protected static final String TAG = "DataAdapter";

    // TODO : TABLE 이름을 명시해야함
    protected static final String TABLE_NAME = "sample_table";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext);
    }

    public DataAdapter createDatabase() throws SQLException
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

    public DataAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase();
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
            // Table 이름 -> antpool_bitcoin 불러오기
            String sql ="SELECT * FROM " + TABLE_NAME;

            // 모델 넣을 리스트 생성
            List placeList = new ArrayList();

            // TODO : 모델 선언
            Data data = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    data = new Data();

                    // TODO : Record 기술
                    // id, name, account, privateKey, secretKey, Comment
                    data.setAccidentCode(mCur.getInt(0));
                    data.setAccidentYear(mCur.getInt(1));
                    data.setAccidentType(mCur.getString(2));
                    data.setPlaceCode(mCur.getInt(3));
                    data.setCityName(mCur.getString(4));
                    data.setPlaceName(mCur.getString(5));
                    data.setAccidentCount(mCur.getInt(6));
                    data.setCasualtiesCount(mCur.getInt(7));
                    data.setDeadCount(mCur.getInt(8));
                    data.setSeriousCount(mCur.getInt(9));
                    data.setSlightlyCount(mCur.getInt(10));
                    data.setInjuredCount(mCur.getInt(11));
                    data.setLatitude(mCur.getFloat(12));
                    data.setLongitude(mCur.getFloat(13));
                    data.setDataDate(mCur.getString(14));

                    // 리스트에 넣기
                    placeList.add(data);
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