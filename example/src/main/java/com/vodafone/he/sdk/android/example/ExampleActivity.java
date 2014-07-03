package com.vodafone.he.sdk.android.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.vodafone.he.sdk.android.UserDetails;
import com.vodafone.he.sdk.android.UserDetailCallback;
import com.vodafone.he.sdk.android.Vodafone;
import com.vodafone.he.sdk.android.VodafoneException;

public class ExampleActivity extends Activity {
    private TextView resolved;
    private TextView stillRunning;
    private TextView source;
    private TextView token;
    private TextView tetheringConflict;
    private TextView secure;
    private TextView validated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        resolved = (TextView) findViewById(R.id.resolved);
        stillRunning = (TextView) findViewById(R.id.stillRunning);
        source = (TextView) findViewById(R.id.source);
        token = (TextView) findViewById(R.id.token);
        tetheringConflict = (TextView) findViewById(R.id.tetheringConflict);
        secure = (TextView) findViewById(R.id.secure);
        validated = (TextView) findViewById(R.id.validated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Vodafone.getUserDetail(new UserDetailCallback() {

            @Override
            public void onSuccess(UserDetails userDetails) {
                resolved.setText(String.valueOf(userDetails.getResolved()));
                stillRunning.setText(String.valueOf(userDetails.getStillRunning()));
                source.setText(userDetails.getSource());
                token.setText(userDetails.getToken());
                tetheringConflict.setText(String.valueOf(userDetails.getTetheringConflict()));
                secure.setText(String.valueOf(userDetails.getSecure()));
                validated.setText(String.valueOf(userDetails.getToken()));
            }

            @Override
            public void onError(VodafoneException ex) {
                Toast.makeText(ExampleActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
