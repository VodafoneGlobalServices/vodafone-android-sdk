package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.vodafone.global.sdk.*;
import com.vodafone.global.sdk.testapp.logging.ui.LogFragment;
import timber.log.Timber;

public class MainActivity extends Activity {
    @InjectView(R.id.et_msisdn) EditText msisdn;
    @InjectView(R.id.cb_smsValidation) CheckBox smsValidation;
    @InjectView(R.id.et_smsCode) EditText smsCode;

    private boolean sdkHasBeenInitialized = false;

    private static final String FRAGMENT_LOG =
            "com.vodafone.global.sdk.testapp.MainActivity.LOG";

    private ResolveCallbackImpl resolveCallback;
    private ValidateSmsCallbackImpl validateSmsCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.testapp.R.layout.activity_main);
        ButterKnife.inject(this);
        setTitle(getTitle() + " v" + BuildConfig.VERSION_NAME);

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
    protected void onStart() {
        super.onStart();
        if (sdkHasBeenInitialized) {
            Vodafone.register(resolveCallback);
            Vodafone.register(validateSmsCallback);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("sdkHasBeenInitialized", sdkHasBeenInitialized);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        sdkHasBeenInitialized = savedInstanceState.getBoolean("sdkHasBeenInitialized");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sdkHasBeenInitialized) {
            Vodafone.unregister(resolveCallback);
            Vodafone.unregister(validateSmsCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_init) {
            new InitDialog().show(getFragmentManager(), "init_fragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_init)
    public void init() {
        Timber.d("init data button clicked");

        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appKey = preferences.getString(Preferences.APP_KEY, Preferences.APP_KEY_DEFAULT);
        String appSecret = preferences.getString(Preferences.APP_SECRET, Preferences.APP_SECRET_DEFAULT);
        String backendAppKey = preferences.getString(Preferences.BACKEND_APP_KEY, Preferences.BACKEND_APP_KEY_DEFAULT);

        Vodafone.init(getApplication(), appKey, appSecret, backendAppKey);


        if (!sdkHasBeenInitialized) {
            sdkHasBeenInitialized = true;
            Vodafone.register(resolveCallback);
            Vodafone.register(validateSmsCallback);
        }
    }

    @OnClick(R.id.btn_retrieve)
    public void retrieve() {

        String msisdn = this.msisdn.getText().toString();
        boolean smsValidation = this.smsValidation.isChecked();
        Timber.v("retrieving user data button clicked\nmsisdn: %s\nsms validation: %b",
                msisdn, smsValidation);

        UserDetailsRequestParameters parameters = UserDetailsRequestParameters.builder()
                .msisdn(msisdn).setSmsValidation(smsValidation).build();
        try {
            Vodafone.retrieveUserDetails(parameters);
        } catch (CallThresholdReached e) {
            Timber.w("Threshold reached");
        } catch (NotInitialized e) {
            Timber.e(e.getMessage());
        }
    }

    @OnClick(R.id.btn_generate)
    public void generatePin() {
        Timber.v("generate pin button clicked");
        try {
            Vodafone.generatePin();
        } catch (CallThresholdReached e) {
            Timber.w("Threshold reached");
        } catch (NotInitialized e) {
            Timber.e(e.getMessage());
        }
    }

    @OnClick(R.id.btn_sendSmsCode)
    public void sendSmsCode() {
        String code = smsCode.getText().toString();
        Timber.v("send sms code button clicked\nsms code: %S", code);
        try {
            Vodafone.validateSmsCode(code);
        } catch (CallThresholdReached e) {
            Timber.w("Threshold reached");
        } catch (NotInitialized e) {
            Timber.e(e.getMessage());
        }
    }

    class ResolveCallbackImpl implements ResolveCallback {
        @Override
        public void onCompleted(UserDetails userDetails) {
            Timber.d("ResolveCallback::onCompleted");
            Timber.i(userDetails.toString());
        }

        @Override
        public void onValidationRequired() {
            Timber.d("ResolveCallback::onValidationRequired");
        }

        @Override
        public void onUnableToResolve() {
            Timber.d("ResolveCallback::onUnableToResolve");
        }

        @Override
        public void onError(VodafoneException ex) {
            Timber.e(ex, "ResolveCallback::onError");
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
            Timber.e(ex, "ValidateSmsCallback::onSmsValidationError");
        }

        @Override
        public void onPinGenerationSuccess() {
            Timber.d("ValidateSmsCallback::onPinGenerationSuccess");
        }
    }
}
