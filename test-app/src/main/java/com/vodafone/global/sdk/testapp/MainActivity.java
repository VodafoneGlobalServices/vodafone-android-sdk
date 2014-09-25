package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Vodafone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends Activity {
    @InjectView(R.id.et_appId) EditText appId;
    @InjectView(R.id.et_imsi) EditText imsi;
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
        Timber.v("setting app id");

        String appId = this.appId.getText().toString();
        boolean commitSucceeded = persistAppId(appId);
        if (commitSucceeded) {
            Timber.d("setting app id succeeded: " + appId);
            Application.exit();
        } else {
            Timber.e("setting app id failed: " + appId);
            Toast.makeText(this, "saving app id failed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves application id for later use.
     * @param appId application id
     * @return true if the new values were successfully written to persistent storage
     */
    private boolean persistAppId(String appId) {
        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.APP_ID, appId);
        return editor.commit();
    }

    @OnClick(R.id.btn_retrieve)
    public void retrieve() {
        Timber.v("retrieving user data button clicked");

        UserDetailsRequestParameters parameters = UserDetailsRequestParameters.builder()
                .setSmsValidation(smsValidation.isChecked())
                .build();
        Vodafone.retrieveUserDetails(parameters);
    }

    @OnClick(R.id.btn_sendSmsCode)
    public void sendSmsCode() {
        String code = smsCode.getText().toString();
        Timber.v("send sms code button clicked, sms code: " + code);
        Vodafone.validateSmsCode(code);
    }
}
