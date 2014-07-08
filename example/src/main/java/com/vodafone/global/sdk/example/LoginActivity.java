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
    private TextView source;
    private TextView token;
    private TextView tetheringConflict;
    private TextView secure;
    private TextView validated;
    private Button logInButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.example.R.layout.main);

        resolved = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.resolved);
        stillRunning = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.stillRunning);
        source = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.source);
        token = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.token);
        tetheringConflict = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.tetheringConflict);
        secure = (TextView) findViewById(com.vodafone.global.sdk.example.R.id.secure);
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
        resolved.setText(String.valueOf(userDetails.getResolved()));
        stillRunning.setText(String.valueOf(userDetails.getStillRunning()));
        source.setText(userDetails.getSource());
        token.setText(userDetails.getToken());
        tetheringConflict.setText(String.valueOf(userDetails.getTetheringConflict()));
        secure.setText(String.valueOf(userDetails.getSecure()));
        validated.setText(String.valueOf(userDetails.getToken()));
    }

    @Override
    public void onUserDetailsError(VodafoneException ex) {
        Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}
