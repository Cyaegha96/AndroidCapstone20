package com.mocca.moccaCanary.menu.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static String TAG = "DataBaseHelper"; //Logcat에 출력할 태그이름

    //디바이스 장치에서 데이터베이스의 경로

    // TODO : assets 폴더에 있는 경우 "", 그 외 경로기입
    private static String DB_PATH = "";
    // TODO : assets 폴더에 있는 DB명 또는 별도의 데이터베이스 파일이름
    private String DB_NAME;

    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public DataBaseHelper(Context context, String DB_NAME, int version)
    {
        super(context, DB_NAME, null, 1);// 1은 데이터베이스 버젼
        this.DB_NAME = DB_NAME;
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public void createDataBase() throws IOException
    {
        //데이터베이스가 없으면 asset폴더에서 복사해온다.
        boolean mDataBaseExist = checkDataBase(DB_NAME);
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assests
                copyDataBase(DB_NAME);
                Log.e(TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private boolean checkDataBase(String DB)
    {
        File dbFile = new File(DB_PATH + DB);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());


        return dbFile.exists();

    }

    //assets폴더에서 데이터베이스를 복사한다.
    private void copyDataBase(String DB) throws IOException
    {


        InputStream mInput = mContext.getAssets().open(DB);
        String outFileName = DB_PATH + DB;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();



    }

    public boolean openDataBase(String db_name) throws SQLException
    {
        String mPath = DB_PATH + db_name;

        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close()
    {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
