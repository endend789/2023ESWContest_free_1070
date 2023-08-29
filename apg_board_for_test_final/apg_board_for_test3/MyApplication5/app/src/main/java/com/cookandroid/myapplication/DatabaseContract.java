package com.cookandroid.myapplication;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "data";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_DATE = "date";
    }
}