package com.vodafone.global.sdk.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.vodafone.global.sdk.*;

public class LoginActivity extends Activity implements ResolveCallback
{
    @InjectView(R.id.resolved) TextView resolved;
    @InjectView(R.id.stillRunning) TextView stillRunning;
    @InjectView(R.id.token) TextView token;
    @InjectView(R.id.validated) TextView validated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.inject(this);
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
    public void onCompleted(UserDetails userDetails) {
        token.setText(userDetails.token);
        validated.setText(String.valueOf(userDetails.token));
    }

    @Override
    public void onValidationRequired() {
        requestSendPinConfirmation();
    }

    @Override
    public void onFailed() {
        // TODO inform about failed resolve
    }

    private void requestSendPinConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("SMS verification is required")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Vodafone.generatePin();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onError(VodafoneException ex) {
        Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.log_in)
    public void logIn() {
        // prepare parameters for service
        Vodafone.retrieveUserDetails(UserDetailsRequestParameters.builder().build());
    }
}
