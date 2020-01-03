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
/*
public class NewCelebrationActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView txtPersonName;
    private TextView txtPhone;
    private RadioGroup radioGroup;
    private DatePicker dpDateOfBirthday;
    private SQLiteDBHelper dbHelper;
    private SQLiteDatabase db;
    private RadioButton radioBirthday, radioAnniversary, radioHoliday;
    private ImageButton contactButton;
    private RelativeLayout rlForTxtPersonName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_celebration_layout);

        txtPersonName = (TextView) findViewById(R.id.txtPersonName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        dpDateOfBirthday = (DatePicker) findViewById(R.id.dpDateOfBirthday);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioBirthday = (RadioButton) findViewById(R.id.radioBirthday);
        radioAnniversary = (RadioButton) findViewById(R.id.radioAnniversary);
        radioHoliday = (RadioButton) findViewById(R.id.radioHoliday);
        radioBirthday.setOnClickListener(this);
        radioAnniversary.setOnClickListener(this);
        radioHoliday.setOnClickListener(this);
        contactButton = (ImageButton) findViewById(R.id.contactButton);
        rlForTxtPersonName = (RelativeLayout) findViewById(R.id.rlForTxtPersonName);

        dbHelper = new SQLiteDBHelper(this);
        db = dbHelper.getWritableDatabase();

        txtPersonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && txtPhone.getVisibility() == View.INVISIBLE) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e){
                        Toast.makeText(NewCelebrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    } catch (Exception e) {
                        Toast.makeText(NewCelebrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    case R.id.radioHoliday:
                        celebrationType = 3;
                        break;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(SQLiteDBHelper.KEY_NAME, txtPersonName.getText().toString());

                if (celebrationType == 3 || txtPhone.getText().toString().length() == 0){
                    txtPhone.setText(getResources().getString(R.string.phone_empty));
                    year = 1;
                }
                contentValues.put(SQLiteDBHelper.KEY_PHONE, txtPhone.getText().toString());
                contentValues.put(SQLiteDBHelper.KEY_CELEBRATION_TYPE, celebrationType);
                contentValues.put(SQLiteDBHelper.KEY_YEAR, year);
                contentValues.put(SQLiteDBHelper.KEY_MONTH, month);
                contentValues.put(SQLiteDBHelper.KEY_DAY, day);

                db.insert(SQLiteDBHelper.TABLE_NAME, null, contentValues);

                txtPersonName.setText("");
                txtPhone.setText("");
                radioGroup.check(R.id.radioBirthday);
                onClick(radioBirthday);

                Toast.makeText(NewCelebrationActivity.this, R.string.added_successful, Toast.LENGTH_SHORT).show();
                com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.countForShowAd++;
                if (com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.countForShowAd > com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.clickCountForShowAd){
                    if (com.bogdan.kolomiiets.birthdayreminder.views.MainActivity.mInterstitialAd.isLoaded()){
                        MainActivity.mInterstitialAd.show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(NewCelebrationActivity.this, R.string.noInserted + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(NewCelebrationActivity.this, R.string.empty, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NewCelebrationActivity.this, getString(R.string.no_select_contact), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.radioBirthday:
                changeLayout();
                setFocusDown(false);
                break;
            case R.id.radioAnniversary:
                changeLayout();
                setFocusDown(false);
                break;
            case R.id.radioHoliday:
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
            txtPersonName.setNextFocusDownId(R.id.btnSave);
        } else {
            txtPersonName.setNextFocusDownId(R.id.txtPhone);
            txtPhone.setNextFocusDownId(R.id.btnSave);
        }
    }

    private void changeLayout(){
        txtPersonName.setHint(R.string.PersonName);
        txtPhone.setVisibility(View.VISIBLE);
        if (contactButton.getParent() != rlForTxtPersonName){
            rlForTxtPersonName.addView(contactButton);
        }
        dpDateOfBirthday.findViewById(Resources.getSystem().getIdentifier("year", "id", "android")).setVisibility(View.VISIBLE);
    }
}
*/
