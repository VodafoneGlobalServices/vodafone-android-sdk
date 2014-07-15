package com.vodafone.global.sdk.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.vodafone.global.sdk.UserDetails;
import com.vodafone.global.sdk.UserDetailsRequestParameters;
import com.vodafone.global.sdk.Vodafone;

public class MainActivity extends Activity {
    @InjectView(R.id.et_appId) EditText appId;
    @InjectView(R.id.et_iccid) EditText iccid;
    @InjectView(R.id.et_token) EditText token;
    @InjectView(R.id.cb_smsValidation) CheckBox smsValidation;
    @InjectView(R.id.et_smsCode) EditText smsCode;
    @InjectView(R.id.et_tokenSms) EditText tokenSms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.vodafone.global.sdk.testapp.R.layout.main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_setAppId)
    public void setAppId() {
        boolean commitSucceeded = persistAppId(appId.getText().toString());
        if (commitSucceeded) {
            Application.exit();
        } else {
            Toast.makeText(this, "saving app id failed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves application id for later use.
     * @param appId application id
     * @return true if the new values were successfully written to persistent storage
     */
    private boolean persistAppId(String appId) {
        SharedPreferences preferences = getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.APP_ID, appId);
        return editor.commit();
    }

    @OnClick(R.id.btn_retrieve)
    public void retrieve() {
        UserDetailsRequestParameters parameters = UserDetailsRequestParameters.builder()
                .setSmsValidation(smsValidation.isChecked())
                .build();
        Vodafone.retrieveUserDetails(parameters);
    }

    @OnClick(R.id.btn_getUserDetails)
    public void getUserDetails() {
        UserDetails userDetails = Vodafone.getUserDetails();
        if (userDetails == null) {
            Toast.makeText(this, "user details aren't cached", Toast.LENGTH_LONG).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("User details")
                    .setMessage("resolved: " + userDetails.getResolved()
                            + "\nstill running: " + userDetails.getStillRunning()
                            + "\nsource: " + userDetails.getSource()
                            + "\ntoken: " + userDetails.getToken()
                            + "\nexpires: " + userDetails.getExpires()
                            + "\ntethering conflict: " + userDetails.getTetheringConflict()
                            + "\nvalidated: " + userDetails.getValidated())
                    .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    @OnClick(R.id.btn_sendSmsCode)
    public void sendSmsCode() {
        Vodafone.validateSmsCode(smsCode.getText().toString());
    }
}
