package com.bogdan.kolomiiets.holidayslist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NotificationDetail extends AppCompatActivity implements View.OnClickListener {

    private TextView tvTypeOfCelebration;
    private TextView tvPersonName;
    private TextView tvPhone;
    private TextView tvHowOld;
    private Button btnCall, btnSMS, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_detail_layout);
        Intent intent = getIntent();

        tvTypeOfCelebration = (TextView) findViewById(R.id.tvTypeOfCelebration);
        tvTypeOfCelebration.setText(new CelebrationType().getCelebrationType(this, intent.getIntExtra(SQLiteDBHelper.KEY_CELEBRATION_TYPE, 0)));

        tvPersonName = (TextView) findViewById(R.id.tvPersonName);
        tvPersonName.setText(intent.getStringExtra(SQLiteDBHelper.KEY_NAME));

        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvPhone.setText(intent.getStringExtra(SQLiteDBHelper.KEY_PHONE));

        tvHowOld = (TextView) findViewById(R.id.tvHowOld);
        tvHowOld.setText(String.valueOf(new HowOld().getHowOld(intent.getIntExtra(SQLiteDBHelper.KEY_YEAR, 0),
                                                intent.getIntExtra(SQLiteDBHelper.KEY_MONTH, 0),
                                                intent.getIntExtra(SQLiteDBHelper.KEY_DAY, 0))));

        btnCall = (Button) findViewById(R.id.btnCall);
        btnSMS = (Button) findViewById(R.id.btnSMS);
        btnClose = (Button) findViewById(R.id.btnClose);

        btnCall.setOnClickListener(this);
        btnSMS.setOnClickListener(this);
        btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCall:
                Intent intentCall = new Intent(Intent.ACTION_DIAL);
                intentCall.setData(Uri.parse("tel:" + tvPhone.getText().toString()));
                startActivity(intentCall);
                break;
            case R.id.btnSMS:
                Intent intentSMS = new Intent(Intent.ACTION_SENDTO);
                intentSMS.setData(Uri.parse("sms:" + tvPhone.getText().toString()));
                startActivity(intentSMS);
                break;
            case R.id.btnClose:
                finish();
                break;
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
