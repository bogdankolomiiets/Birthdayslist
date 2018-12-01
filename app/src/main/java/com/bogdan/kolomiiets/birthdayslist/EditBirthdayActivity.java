package com.bogdan.kolomiiets.birthdayslist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EditBirthdayActivity extends AppCompatActivity {
    private SQLiteDBHelper helper;
    private SQLiteDatabase db;
    private TextView txtPersonName;
    private TextView txtPhone;
    private RadioGroup radioGroup;
    private DatePicker dpDateOfBirthday;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_birthday_layout);
        helper = new SQLiteDBHelper(EditBirthdayActivity.this);
        db = helper.getWritableDatabase();
        txtPersonName = (TextView) findViewById(R.id.txtPersonName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        dpDateOfBirthday = (DatePicker) findViewById(R.id.dpDateOfBirthday);

        txtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e){
                        Toast.makeText(EditBirthdayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);

        Cursor cursor = db.query(SQLiteDBHelper.TABLE_NAME, SQLiteDBHelper.COLUMNS_NAMES, SQLiteDBHelper.KEY_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        txtPersonName.setText(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_NAME)));
        txtPhone.setText(cursor.getString(cursor.getColumnIndex(SQLiteDBHelper.KEY_PHONE)));

        switch (cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_CELEBRATION_TYPE))) {
            case 1:
                radioGroup.check(R.id.radioBirthday);
                break;
            case 2:
                radioGroup.check(R.id.radioAnniversary);
                break;
            case 3:
                radioGroup.check(R.id.radioAnother);
                break;
        }

        dpDateOfBirthday.updateDate(cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_YEAR)), cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_MONTH)) - 1, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_DAY)));
        cursor.close();
    }


    public void onCancelClick(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        db.close();
        helper.close();
        Intent intent = new Intent(EditBirthdayActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSaveClick(View view){
        if (txtPersonName.getText().toString().length() > 0) {
            try {
            ContentValues values = new ContentValues();
            values.put(SQLiteDBHelper.KEY_NAME, txtPersonName.getText().toString());
            values.put(SQLiteDBHelper.KEY_PHONE, txtPhone.getText().toString());

            int radio = 0;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radioBirthday:
                    radio = 1;
                    break;
                case R.id.radioAnniversary:
                    radio = 2;
                    break;
                case R.id.radioAnother:
                    radio = 3;
                    break;
            }
            values.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, radio);

            int year = dpDateOfBirthday.getYear();
            int month = dpDateOfBirthday.getMonth() + 1;
            int day = dpDateOfBirthday.getDayOfMonth();
            values.put(SQLiteDBHelper.KEY_YEAR, year);
            values.put(SQLiteDBHelper.KEY_MONTH, month);
            values.put(SQLiteDBHelper.KEY_DAY, day);
            int res = db.update(SQLiteDBHelper.TABLE_NAME, values, SQLiteDBHelper.KEY_ID + "=" + id, null);
            if (res > 0) {
                Toast.makeText(EditBirthdayActivity.this, getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                db.close();
                helper.close();
                Intent intent = new Intent(EditBirthdayActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception ex){
                Toast.makeText(view.getContext(), R.string.noInserted + "\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(view.getContext(), R.string.empty, Toast.LENGTH_SHORT).show();
        }
    }

    public void openContacts(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, RequestCodes.PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCodes.PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    Uri contact = data.getData();
                    Cursor cursor = managedQuery(contact, null, null, null, null);
                    cursor.moveToFirst();
                    txtPersonName.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    txtPhone.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                } else {
                    Toast.makeText(EditBirthdayActivity.this, getString(R.string.noSelectContact), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
