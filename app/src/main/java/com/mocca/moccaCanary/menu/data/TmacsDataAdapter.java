package com.mocca.moccaCanary.menu.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TmacsDataAdapter {

    protected static final String TAG = "tmacsDataAdapter";
    private static String DB_NAME_TMACS= "tmacs_data.db";
    // TODO : TABLE 이름을 명시해야함


    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public TmacsDataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext, DB_NAME_TMACS, 1);
    }

    public TmacsDataAdapter createDatabase() throws SQLException
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

    public TmacsDataAdapter open() throws SQLException
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

    public List getTableData(String Region)
    {
        try
        {
            String sql = "SELECT * FROM " + Region;
            Log.d(TAG,"SELECT * FROM " + Region);
            // 모델 넣을 리스트 생성
            List placeList = new ArrayList();

            // TODO : 모델 선언
            tmacsData tmacsdata = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null) {
                // 칼럼의 마지막까지
                int index = 1;
                while (mCur.moveToNext()) {

                    // TODO : 커스텀 모델 생성
                    tmacsdata = new tmacsData();

                    // TODO : Record 기술
                    tmacsdata.setRegion(Region);
                    tmacsdata.setDistrict(mCur.getString(0));
                    tmacsdata.setPlaceName(mCur.getString(1));
                    tmacsdata.setAccidentCount(mCur.getInt(2));
                    tmacsdata.setDeadCount(mCur.getInt(3));
                    tmacsdata.setSeriousCount(mCur.getInt(4));
                    tmacsdata.setSlightlyCount(mCur.getInt(5));
                    tmacsdata.setInjuredCount(mCur.getInt(6));
                    tmacsdata.setBlackSpotScore(mCur.getFloat(7));
                    tmacsdata.setSeverityScore(mCur.getFloat(8));
                    tmacsdata.setTotalScore(mCur.getFloat(9));
                    tmacsdata.setAccidentType(mCur.getString(10));
                    tmacsdata.setLatitude(mCur.getFloat(11));
                    tmacsdata.setLongitude(mCur.getFloat(12));
                    tmacsdata.setOldCount(mCur.getInt(13));
                    tmacsdata.setChildCount(mCur.getInt(14));
                    tmacsdata.setIndex(index);
                    // 리스트에 넣기
                    placeList.add( tmacsdata);
                    index++;
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
