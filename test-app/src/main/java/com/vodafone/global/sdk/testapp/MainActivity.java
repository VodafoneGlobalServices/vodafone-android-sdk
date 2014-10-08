package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.vodafone.global.sdk.*;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.vodafone.global.sdk.testapp.logging.ui.LogFragment;
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

    private static final String FRAGMENT_LOG =
            "com.vodafone.global.sdk.testapp.MainActivity.LOG";

    private ResolveCallbackImpl resolveCallback;
    private ValidateSmsCallbackImpl validateSmsCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.testapp.R.layout.main);
        ButterKnife.inject(this);
        readInitData();

        if (savedInstanceState == null) {
            Fragment f = LogFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.log, f, FRAGMENT_LOG)
                    .commit();
        }

        resolveCallback = new ResolveCallbackImpl();
        validateSmsCallback = new ValidateSmsCallbackImpl();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Vodafone.register(resolveCallback);
        Vodafone.register(validateSmsCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Vodafone.register(resolveCallback);
        Vodafone.register(validateSmsCallback);
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

    class ResolveCallbackImpl implements ResolveCallback {
        @Override
        public void onCompleted(UserDetails userDetails) {
            Timber.d("ResolveCallback::onCompleted");
        }

        @Override
        public void onValidationRequired() {
            Timber.d("ResolveCallback::onValidationRequired");
        }

        @Override
        public void onFailed() {
            Timber.d("ResolveCallback::onFailed");
        }

        @Override
        public void onError(VodafoneException ex) {
            Timber.d("ResolveCallback::onError");
        }
    }

    class ValidateSmsCallbackImpl implements ValidateSmsCallback {
        @Override
        public void onSmsValidationSuccessful() {
            Timber.d("ValidateSmsCallback::onSmsValidationSuccessful");
        }

        @Override
        public void onSmsValidationFailure() {
            Timber.d("ValidateSmsCallback::onSmsValidationFailure");
        }

        @Override
        public void onSmsValidationError(VodafoneException ex) {
            Timber.d("ValidateSmsCallback::onSmsValidationError");
        }

        @Override
        public void onPinGenerationSuccess() {
            Timber.d("ValidateSmsCallback::onPinGenerationSuccess");
        }
    }
}
