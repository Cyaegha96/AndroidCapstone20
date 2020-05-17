package com.example.mynavigator.ui.data;

import android.content.ContentValues;
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
    private static String DB_NAME = "data_all.db";
    // TODO : TABLE 이름을 명시해야함
    protected static final String TABLE_NAME = "sample_table";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    private final String COS_LATITUDE = "cos_latitude";
    private final String COS_LONGITUDE = "cos_longitude";
    private final String SIN_LATITUDE = "sin_latitude";
    private final String SIN_LONGITUDE = "sin_longitude";



    public DataAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext, DB_NAME, 1);
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
            mDbHelper.openDataBase(DB_NAME);
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
            // 모델 넣을 리스트 생성
            List placeList = new ArrayList();

            // TODO : 모델 선언
            Data data = null;

            Cursor mCur = mDb.rawQuery("SELECT * FROM "+TABLE_NAME, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    data = new Data();

                    // TODO : Record 기술
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

    //인자로 들어온 latitude와 logtitude를 이용해 쿼리문의 일부를 만듭니다.
    private String buildDistanceQuery(double latitude, double longitude) {

        final double sinLat = Math.sin(Math.toRadians(latitude));
        final double cosLat = Math.cos(Math.toRadians(latitude));
        final double sinLng = Math.sin(Math.toRadians(longitude));
        final double cosLng = Math.cos(Math.toRadians(longitude));

        return "(" + cosLat + "*" + COS_LATITUDE
                + "*(" + COS_LONGITUDE + "*" + cosLng
                + "+" + SIN_LONGITUDE + "*" + sinLng
                + ")+" + sinLat + "*" + SIN_LATITUDE
                + ")";
    }


    public void addMathData(Data data) {

        mDb = mDbHelper.getReadableDatabase();

        double lat = data.getLatitude();
        double lng = data.getLongitude();

        ContentValues values = new ContentValues();
        values.put(SIN_LATITUDE, Math.sin(Math.toRadians(lat)));
        values.put(SIN_LONGITUDE, Math.sin(Math.toRadians(lng)));
        values.put(COS_LATITUDE, Math.cos(Math.toRadians(lat)));
        values.put(COS_LONGITUDE, Math.cos(Math.toRadians(lng)));
        Log.d(TAG,"insert  "+Math.sin(Math.toRadians(lat)) +" " +
                " " +Math.sin(Math.toRadians(lng))+ " "+  Math.cos(Math.toRadians(lat))+" " + Math.cos(Math.toRadians(lng)) );
       long result =  mDb.insert(TABLE_NAME, null, values);
       if(result == -1){
           Log.d(TAG,"insert 실패");
       }else{
           Log.d(TAG,"insert 성공");
       }
        mDb.close();
    }


    public List<Data> getList(double latitude, double longitude, double distance) {

        String sql = "SELECT *" + ", " + buildDistanceQuery(latitude, longitude)
                + " AS partial_distance"
                + " FROM " + TABLE_NAME
                + " WHERE partial_distance >= "
                + Math.cos(distance/6371);
        try
        {

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