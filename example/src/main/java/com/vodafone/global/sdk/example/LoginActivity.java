package com.vodafone.global.sdk.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.UserDetailsCallback;
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Vodafone;
import com.vodafone.global.sdk.VodafoneException;

import butterknife.InjectView;
import butterknife.OnClick;

public class LoginActivity extends Activity implements UserDetailsCallback
{
    @InjectView(R.id.resolved) TextView resolved;
    @InjectView(R.id.stillRunning) TextView stillRunning;
    @InjectView(R.id.token) TextView token;
    @InjectView(R.id.validated) TextView validated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start listening to UserDetails changes
        Vodafone.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop listening to UserDetails changes
        Vodafone.unregister(this);
    }

    @Override
    public void onUserDetailsUpdate(UserDetails userDetails) {
        resolved.setText(String.valueOf(userDetails.resolved));
        stillRunning.setText(String.valueOf(userDetails.stillRunning));
        token.setText(userDetails.token);
        validated.setText(String.valueOf(userDetails.token));
    }

    @Override
    public void onUserDetailsError(VodafoneException ex) {
        Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.log_in)
    public void logIn() {
        // prepare parameters for service
        Vodafone.retrieveUserDetails(
                UserDetailsRequestParameters.builder()
                        .setSmsValidation(false)
                        .build());
    }
}
