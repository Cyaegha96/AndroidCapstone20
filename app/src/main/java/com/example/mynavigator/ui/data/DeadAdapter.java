package com.example.mynavigator.ui.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeadAdapter {

    protected static final String TAG = "DeadDataAdapter";
    private static String DB_NAME_DD ="dead_db.db";
    // TODO : TABLE 이름을 명시해야함
    protected static final String TABLE_NAME = "dead_table";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseHelper mDbHelper;

    public DeadAdapter(Context context)
    {
        this.mContext = context;
        mDbHelper = new DataBaseHelper(mContext, DB_NAME_DD, 1);
    }

    public DeadAdapter createDatabase() throws SQLException
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

    public DeadAdapter open() throws SQLException
    {
        try
        {
            mDbHelper.openDataBase(DB_NAME_DD);
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
            DeadData deaddata = null;

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                // 칼럼의 마지막까지
                while( mCur.moveToNext() ) {

                    // TODO : 커스텀 모델 생성
                    deaddata = new DeadData();

                    // TODO : Record 기술
                    // id, name, account, privateKey, secretKey, Comment
                    deaddata.setAcc_year(mCur.getInt(0));
                    deaddata.setOccrrnc_dt(mCur.getInt(1));
                    deaddata.setDght_cd(mCur.getString(2));
                    deaddata.setDth_dnv_cnt(mCur.getInt(3));
                    deaddata.setInjpsn_cnt(mCur.getInt(4));
                    deaddata.setSe_dnv_cnt(mCur.getInt(5));
                    deaddata.setSl_dnv_cnt(mCur.getInt(6));
                    deaddata.setWnd_dnv_cnt(mCur.getInt(7));
                    deaddata.setOccrrnc_lc_sgg_cd(mCur.getString(8));
                    deaddata.setAcc_ty_cd(mCur.getString(9));
                    deaddata.setAslt_vtr_lclas_cd(mCur.getString(10));
                    deaddata.setAslt_vtr_mlsfc_cd(mCur.getString(11));
                    deaddata.setRoad_frm_lclas_cd(mCur.getString(12));
                    deaddata.setRoad_frm_cd(mCur.getString(13));
                    deaddata.setWrngdo_isrty_vhcty_lclas_cd(mCur.getString(14));
                    deaddata.setWrngdo_isrty_vhcty_cd(mCur.getString(15));
                    deaddata.setLa_crd(mCur.getFloat(16));
                    deaddata.setLo_crd(mCur.getFloat(17));


                    // 리스트에 넣기
                    placeList.add(deaddata);
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
