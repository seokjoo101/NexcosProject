package com.example.seokjoo.contactex;

import android.provider.BaseColumns;

/**
 * Created by Seokjoo on 2016-08-08.
 */
public class DataBases {
    public static final class CreateDB implements BaseColumns {

        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String _TABENAME = "nexcos";

        public static final String _CREATE =
                "create table "+_TABENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +NAME+" text not null , "
                        +PHONE+" text not null );"  ;
    }
}
