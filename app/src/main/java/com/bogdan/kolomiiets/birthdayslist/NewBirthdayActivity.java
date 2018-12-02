package com.bogdan.kolomiiets.birthdayslist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class NewBirthdayActivity extends AppCompatActivity {

    private TextView txtPersonName;
    private TextView txtPhone;
    private RadioGroup radioGroup;
    private DatePicker dpDateOfBirthday;
    private SQLiteDBHelper dbHelper;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_birthday_layout);

        txtPersonName = (TextView) findViewById(R.id.txtPersonName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        dpDateOfBirthday = (DatePicker) findViewById(R.id.dpDateOfBirthday);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        dbHelper = new SQLiteDBHelper(this);
        db = dbHelper.getWritableDatabase();

        txtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e) {
                        Toast.makeText(NewBirthdayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    public void onAddClick(View view) {
        if (txtPersonName.getText().toString().length() > 0) {
            try {
                int day = dpDateOfBirthday.getDayOfMonth();
                int month = dpDateOfBirthday.getMonth() + 1;
                int year = dpDateOfBirthday.getYear();

                int celebrationType = 0;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioBirthday:
                        celebrationType = 1;
                        break;
                    case R.id.radioAnniversary:
                        celebrationType = 2;
                        break;
                    case R.id.radioAnother:
                        celebrationType = 3;
                        break;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(SQLiteDBHelper.KEY_NAME, txtPersonName.getText().toString());
                contentValues.put(SQLiteDBHelper.KEY_PHONE, txtPhone.getText().toString());
                contentValues.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, celebrationType);
                contentValues.put(SQLiteDBHelper.KEY_YEAR, year);
                contentValues.put(SQLiteDBHelper.KEY_MONTH, month);
                contentValues.put(SQLiteDBHelper.KEY_DAY, day);

                db.insert(SQLiteDBHelper.TABLE_NAME, null, contentValues);

                txtPersonName.setText("");
                txtPhone.setText("");
                radioGroup.check(R.id.radioBirthday);

                Toast.makeText(NewBirthdayActivity.this, R.string.insertedSuccessful, Toast.LENGTH_SHORT).show();
                MainActivity.countForAd++;
                if (MainActivity.countForAd > 7){
                    if (MainActivity.mInterstitialAd.isLoaded()){
                        MainActivity.mInterstitialAd.show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(NewBirthdayActivity.this, R.string.noInserted + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(NewBirthdayActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    public void onCloseClick(View view) {
        closeActivity();
    }

    private void closeActivity()
    {
        //Close database
        db.close();
        //Show MainActivity
        /*Intent intent = new Intent(NewBirthdayActivity.this, MainActivity.class);
        startActivity(intent);*/
        finish();
    }


    public void openContacts(View view) {
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
                    Cursor c = managedQuery(contact, null, null, null, null);
                    c.moveToFirst();
                    txtPersonName.setText(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    txtPhone.setText(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                } else {
                    Toast.makeText(NewBirthdayActivity.this, getString(R.string.noSelectContact), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

