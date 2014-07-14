package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity {
    @InjectView(R.id.et_appId) EditText appId;
    @InjectView(R.id.et_iccid) EditText iccid;
    @InjectView(R.id.et_token) EditText token;
    @InjectView(R.id.cb_smsValidation) CheckBox smsValidation;
    @InjectView(R.id.et_smsCode) EditText smsCode;
    @InjectView(R.id.et_tokenSms) EditText tokenSms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.testapp.R.layout.main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_setAppId)
    public void setAppId() {
    }

    @OnClick(R.id.btn_retrieve)
    public void retrieve() {
    }

    @OnClick(R.id.btn_getUserDetails)
    public void getUserDetails() {
    }

    @OnClick(R.id.btn_sendSmsCode)
    public void sendSmsCode() {
    }
}
