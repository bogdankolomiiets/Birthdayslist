package com.bogdan.kolomiiets.birthdayslist;

import java.util.HashMap;

public class DataForListView extends HashMap<String, String> {

    DataForListView(String id, String name, String phone, String type, String age, String date){
        super();
        super.put(SQLiteDBHelper.KEY_ID, id);
        super.put(SQLiteDBHelper.KEY_NAME, name);
        super.put(SQLiteDBHelper.KEY_PHONE, phone);
        super.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, type);
        super.put("date", date);
        super.put("age", age);
    }
}
