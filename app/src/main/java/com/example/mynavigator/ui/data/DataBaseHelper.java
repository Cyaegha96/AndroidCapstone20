package com.example.mynavigator.ui.data;

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
import java.sql.SQLInput;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static String TAG = "DataBaseHelper"; //Logcat에 출력할 태그이름

    //디바이스 장치에서 데이터베이스의 경로

    // TODO : assets 폴더에 있는 경우 "", 그 외 경로기입
    private static String DB_PATH = "";
    // TODO : assets 폴더에 있는 DB명 또는 별도의 데이터베이스 파일이름
    private static String DB_NAME ="sample_db.db";
    private static String DB_NAME_CW ="crosswalk_db.db";
    private static String DB_NAME_DD ="dead_db.db";

    private SQLiteDatabase mDataBase;
    private SQLiteDatabase cwDataBase;
    private SQLiteDatabase ddDataBase;
    private final Context mContext;

    public DataBaseHelper(Context context, String DB_NAME, int version)
    {
        super(context, DB_NAME, null, 1);// 1은 데이터베이스 버젼
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
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    ///data/data/your package/databases/Da Name <-이 경로에서 데이터베이스가 존재하는지 확인한다
    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DB_NAME);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());

        File cwFile = new File(DB_PATH + DB_NAME_CW);

        File deadFile = new File(DB_PATH + DB_NAME_DD);

        return dbFile.exists()&&cwFile.exists()&&deadFile.exists();

    }

    //assets폴더에서 데이터베이스를 복사한다.
    private void copyDataBase() throws IOException
    {


        InputStream cwInput = mContext.getAssets().open(DB_NAME_CW);
        String cwFileName = DB_PATH + DB_NAME_CW;
        OutputStream cwOutput = new FileOutputStream(cwFileName);
        byte[] cwBuffer = new byte[1024];
        int cwLength;
        while ((cwLength = cwInput.read(cwBuffer))>0)
        {
            cwOutput.write(cwBuffer, 0, cwLength);
        }
        cwOutput.flush();
        cwOutput.close();
        cwInput.close();

        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
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


        InputStream dInput = mContext.getAssets().open(DB_NAME_DD);
        String doutFileName = DB_PATH + DB_NAME_DD;
        OutputStream dOutput = new FileOutputStream(doutFileName);
        byte[] dBuffer = new byte[1024];
        int dLength;
        while ((dLength = dInput.read(dBuffer))>0)
        {
            dOutput.write(dBuffer, 0, dLength);
        }
        dOutput.flush();
        dOutput.close();
        dInput.close();
    }

    //데이터베이스를 열어서 쿼리를 쓸수있게만든다.
    public boolean openDataBase(String db_name) throws SQLException
    {
        String mPath = DB_PATH + db_name;

        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close()
    {
        if(mDataBase != null)
            mDataBase.close();

        if(cwDataBase != null)
            cwDataBase.close();

        if(ddDataBase != null)
            cwDataBase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
