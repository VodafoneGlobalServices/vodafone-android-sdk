package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.app.Fragment;
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

    private static final String FRAGMENT_LOG =
            "com.vodafone.global.sdk.testapp.MainActivity.LOG";

    private ResolveCallbackImpl resolveCallback;
    private ValidateSmsCallbackImpl validateSmsCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.testapp.R.layout.activity_main);
        ButterKnife.inject(this);

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

    @OnClick(R.id.btn_retrieve)
    public void retrieve() {

        String msisdn = this.msisdn.getText().toString();
        boolean smsValidation = this.smsValidation.isChecked();
        Timber.v("retrieving user data button clicked\nmsisdn: %s\nsms validation: %b",
                msisdn, smsValidation);

        UserDetailsRequestParameters parameters = UserDetailsRequestParameters.builder()
                .msisdn(msisdn).setSmsValidation(smsValidation).build();
        Vodafone.retrieveUserDetails(parameters);
    }

    @OnClick(R.id.btn_generate)
    public void generatePin() {
        Timber.v("generate pin button clicked");
        Vodafone.generatePin();
    }

    @OnClick(R.id.btn_sendSmsCode)
    public void sendSmsCode() {
        String code = smsCode.getText().toString();
        Timber.v("send sms code button clicked\nsms code: %S", code);
        Vodafone.validateSmsCode(code);
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
            Timber.d("ResolveCallback::onError");
            Timber.e(ex, ex.getMessage());
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
            Timber.e(ex, ex.getMessage());
        }

        @Override
        public void onPinGenerationSuccess() {
            Timber.d("ValidateSmsCallback::onPinGenerationSuccess");
        }
    }
}
