package com.bns.localtransfer;

import android.content.Context;

public class MyDatabase {
    private AppDatabase appDatabase;
    private static MyDatabase instance;

    public static void init(Context context) {
        instance = new MyDatabase();
        instance.appDatabase = AppDatabase.getInstance(context);
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }
    public static Dao dao(){
        return MyDatabase.getInstance().appDatabase.dao();
    }
}
