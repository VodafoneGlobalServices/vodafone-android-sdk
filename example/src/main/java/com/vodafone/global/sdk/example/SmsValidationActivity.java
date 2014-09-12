package com.vodafone.global.sdk.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.vodafone.global.sdk.ValidateSmsCallback;
import com.vodafone.global.sdk.Vodafone;
import com.vodafone.global.sdk.VodafoneException;

public class SmsValidationActivity extends Activity implements ValidateSmsCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.validate_sms);

        final EditText smsCodeField = (EditText) findViewById(R.id.smsCode);

        Button validate = (Button) findViewById(R.id.validate);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smsCode = smsCodeField.getText().toString();
                String token = "Token"; //TODO add token
                Vodafone.validateSmsCode(token, smsCode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Vodafone.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Vodafone.unregister(this);
    }

    @Override
    public void onSmsValidationSuccessful() {
        // handle successful SMS validation
    }

    @Override
    public void onSmsValidationFailure() {
        // handle unsuccessful SMS validation
    }

    @Override
    public void onSmsValidationError(VodafoneException ex) {
        // handle error
    }
}
