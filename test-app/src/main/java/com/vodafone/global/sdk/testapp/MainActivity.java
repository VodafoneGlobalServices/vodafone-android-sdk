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
    @InjectView(R.id.et_appKey) EditText appKeyET;
    @InjectView(R.id.et_appSecret) EditText appSecretET;
    @InjectView(R.id.et_backendAppKey) EditText backendAppKeyET;
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
        readInitData();
    }

    private void readInitData() {
        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appKey = preferences.getString(Preferences.APP_KEY, "");
        String appSecret = preferences.getString(Preferences.APP_SECRET, "");
        String backendAppKey = preferences.getString(Preferences.BACKEND_APP_KEY, "");

        appKeyET.setText(appKey);
        appSecretET.setText(appSecret);
        backendAppKeyET.setText(backendAppKey);
    }

    @OnClick(R.id.btn_setInitData)
    public void setAppId() {
        String appKey = appKeyET.getText().toString();
        String appSecret = appSecretET.getText().toString();
        String backendAppKey = backendAppKeyET.getText().toString();
        boolean commitSucceeded = persistInitData(appKey, appSecret, backendAppKey);
        if (commitSucceeded) {
            Timber.d("setting init data succeeded; app key: '%s', app secret: '%s', backend key: '%s'",
                    appKey, appSecret, backendAppKey);
            Application.exit();
        } else {
            Timber.e("setting init data failed; app key: '%s', app secret: '%s', backend key: '%s'",
                    appKey, appSecret, backendAppKey);
            Toast.makeText(this, "saving app id failed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves SDK init data for later use.
     * @return true if the new values were successfully written to persistent storage
     */
    private boolean persistInitData(String appId, String appSecret, String backendAppKey) {
        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.APP_KEY, appId);
        editor.putString(Preferences.APP_SECRET, appSecret);
        editor.putString(Preferences.BACKEND_APP_KEY, backendAppKey);
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
