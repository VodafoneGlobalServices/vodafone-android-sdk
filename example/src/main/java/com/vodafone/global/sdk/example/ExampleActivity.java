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
import timber.log.Timber;

public class ExampleActivity extends Activity implements ResolveCallback, ValidateSmsCallback
{
    @InjectView(R.id.tokenId) TextView tokenId;
    @InjectView(R.id.expires) TextView expires;
    @InjectView(R.id.acr) TextView acr;

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
        Timber.d("completed %s", userDetails.toString());
        tokenId.setText(userDetails.tokenId);
        expires.setText(userDetails.expires.toString());
        acr.setText(userDetails.acr);
    }

    @Override
    public void onValidationRequired() {
        Timber.d("validation required");
        requestSendPinConfirmation();
    }

    private void requestSendPinConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("SMS verification is required")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.i("SMS generation confirmed");
                        Vodafone.generatePin();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.i("SMS generation canceled");
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onUnableToResolve() {
        Timber.d("ExampleActivity.onUnableToResolve");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("Resolution failed")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onError(VodafoneException ex) {
        Timber.e(ex, "");
        Toast.makeText(ExampleActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.log_in_without_msisdn)
    public void logInWithoutMsisdn() {
        Timber.i("start resolve without MSISDN");
        Vodafone.retrieveUserDetails(UserDetailsRequestParameters.builder().build());
    }

    @OnClick(R.id.log_in_with_msisdn)
    public void logWithMsisdn() {
        Timber.i("start resolve with MSISDN");

    }

    @Override
    public void onSmsValidationSuccessful() {
        Timber.i("onSmsValidationSuccessful");
    }

    @Override
    public void onSmsValidationFailure() {
        Timber.i("onSmsValidationFailure");
    }

    @Override
    public void onSmsValidationError(VodafoneException ex) {
        Timber.e(ex, "onSmsValidationError");
    }

    @Override
    public void onPinGenerationSuccess() {
        Timber.i("onPinGenerationSuccess");
    }
}
