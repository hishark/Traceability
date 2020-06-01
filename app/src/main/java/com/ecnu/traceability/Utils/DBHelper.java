package com.ecnu.traceability.Utils;

import android.content.Context;

import com.ecnu.traceability.bluetooth.Dao.DaoMaster;
import com.ecnu.traceability.bluetooth.Dao.DaoSession;


public class DBHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static DBHelper mInstance;
    private DaoMaster.DevOpenHelper mOpenHelper;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private String password = "AskSky_TanPeiQi_1195211669_JMSQJ";
    private static final String DBName = "testDB";

    private DBHelper() {
    }

    public static DBHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DBHelper();
        }
        return mInstance;
    }

    public void init(Context context) {
        mOpenHelper = new DaoMaster.DevOpenHelper(context, DBName, null);
//        mDaoMaster = new DaoMaster(mOpenHelper.getEncryptedWritableDb(Utils.getMd5(password)));
        mDaoMaster = new DaoMaster(mOpenHelper.getWritableDb());

        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

}