package com.vodafone.global.sdk.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.vodafone.global.sdk.*;

public class LoginActivity extends Activity implements UserDetailsCallback
{
    private TextView resolved;
    private TextView stillRunning;
    private TextView token;
    private TextView validated;
    private Button logInButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.example.R.layout.main);

        resolved = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.resolved);
        stillRunning = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.stillRunning);
        token = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.token);
        validated = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.validated);

        logInButton = (Button) findViewById(com.vodafone.global.sdk.example.R.id.log_in);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prepare parameters for service
                UserDetailsRequestParameters options = UserDetailsRequestParameters.builder()
                        .enableSmsValidation()
                        .build();
                Vodafone.retrieveUserDetails(options);
            }
        });
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
}
