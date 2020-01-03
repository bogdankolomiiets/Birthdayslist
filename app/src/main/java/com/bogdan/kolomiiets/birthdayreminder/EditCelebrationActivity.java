package com.bogdan.kolomiiets.birthdayreminder;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bogdan.kolomiiets.birthdayreminder.views.MainActivity;

import java.util.Calendar;
/*
public class EditCelebrationActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDBHelper helper;
    private SQLiteDatabase db;
    private TextView txtPersonName;
    private TextView txtPhone;
    private RadioGroup radioGroup;
    private DatePicker dpDateOfBirthday;
    private int id;
    private RadioButton radioBirthday, radioAnniversary, radioHoliday;
    private ImageButton contactButton;
    private RelativeLayout rlForTxtPersonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_or_update_event);
        helper = new SQLiteDBHelper(EditCelebrationActivity.this);
        db = helper.getWritableDatabase();
        txtPersonName = (TextView) findViewById(R.id.person_name);
        txtPhone = (TextView) findViewById(R.id.phone);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        dpDateOfBirthday = (DatePicker) findViewById(R.id.date_picker);
        radioBirthday = (RadioButton) findViewById(R.id.radio_birthday);
        radioAnniversary = (RadioButton) findViewById(R.id.radio_anniversary);
        radioHoliday = (RadioButton) findViewById(R.id.radio_holiday);
        radioBirthday.setOnClickListener(this);
        radioAnniversary.setOnClickListener(this);
        radioHoliday.setOnClickListener(this);
        contactButton = (ImageButton) findViewById(R.id.pick_contact_btn);
        rlForTxtPersonName = (RelativeLayout) findViewById(R.id.rlForTxtPersonName);

        txtPersonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && txtPhone.getVisibility() == View.INVISIBLE) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e){
                        Toast.makeText(EditCelebrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        txtPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e){
                        Toast.makeText(EditCelebrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                radioGroup.check(R.id.radio_birthday);
                break;
            case 2:
                radioGroup.check(R.id.radio_anniversary);
                break;
            case 3:
                radioGroup.check(R.id.radio_holiday);
                onClick((View)findViewById(R.id.radio_holiday));
                break;
        }

        int year = cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_YEAR));

        if (year == 1){
            year = 2000;
        }
        dpDateOfBirthday.updateDate(year, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_MONTH)) - 1, cursor.getInt(cursor.getColumnIndex(SQLiteDBHelper.KEY_DAY)));
        cursor.close();
    }


    public void onCancelClick(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        db.close();
        helper.close();
        Intent intent = new Intent(EditCelebrationActivity.this, com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSaveClick(View view) {
        if (txtPersonName.getText().toString().length() > 0) {
            try {
                ContentValues values = new ContentValues();

                int radio = 0;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_birthday:
                        radio = 1;
                        break;
                    case R.id.radio_anniversary:
                        radio = 2;
                        break;
                    case R.id.radio_holiday:
                        radio = 3;
                        break;
                }
                int year = dpDateOfBirthday.getYear();
                int month = dpDateOfBirthday.getMonth() + 1;
                int day = dpDateOfBirthday.getDayOfMonth();

                values.put(SQLiteDBHelper.KEY_NAME, txtPersonName.getText().toString());

                if (radio == 3 || txtPhone.getText().toString().length() == 0){
                    txtPhone.setText(getResources().getString(R.string.phone_empty));
                    year = 1;
                }

                values.put(SQLiteDBHelper.KEY_PHONE, txtPhone.getText().toString());
                values.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, radio);
                values.put(SQLiteDBHelper.KEY_YEAR, year);
                values.put(SQLiteDBHelper.KEY_MONTH, month);
                values.put(SQLiteDBHelper.KEY_DAY, day);
                int res = db.update(SQLiteDBHelper.TABLE_NAME, values, SQLiteDBHelper.KEY_ID + "=" + id, null);
                if (res > 0) {
                    Toast.makeText(EditCelebrationActivity.this, getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                    db.close();
                    helper.close();
                    Intent intent = new Intent(EditCelebrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception ex) {
                Toast.makeText(view.getContext(), R.string.noInserted + "\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(EditCelebrationActivity.this, getString(R.string.no_select_contact), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.radio_birthday:
                changeLayout();
                setFocusDown(false);
                break;
            case R.id.radio_anniversary:
                changeLayout();
                setFocusDown(false);
                break;
            case R.id.radio_holiday:
                setFocusDown(true);
                txtPersonName.setHint(R.string.holiday_name);
                rlForTxtPersonName.removeView(contactButton);
                txtPhone.setVisibility(View.INVISIBLE);
                dpDateOfBirthday.findViewById(Resources.getSystem().getIdentifier("year", "id", "android")).setVisibility(View.GONE);
                break;
        }
    }

    private void setFocusDown(boolean isRadioHoliday){
        if (isRadioHoliday){
            txtPersonName.setNextFocusDownId(R.id.save_btn);
        } else {
            txtPersonName.setNextFocusDownId(R.id.phone);
            txtPhone.setNextFocusDownId(R.id.save_btn);
        }
    }

    private void changeLayout(){
        txtPersonName.setHint(R.string.PersonName);
        txtPhone.setVisibility(View.VISIBLE);
        if (contactButton.getParent() != rlForTxtPersonName){
            rlForTxtPersonName.addView(contactButton);
        }
        dpDateOfBirthday.findViewById(Resources.getSystem().getIdentifier("year", "id", "android")).setVisibility(View.VISIBLE);
        Calendar calendar = Calendar.getInstance();
        dpDateOfBirthday.updateDate(calendar.get(Calendar.YEAR), dpDateOfBirthday.getMonth(), dpDateOfBirthday.getDayOfMonth());
    }
}

 */
