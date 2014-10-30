package com.vodafone.global.sdk.testapp;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class InitDialog extends DialogFragment {
    @InjectView(R.id.et_appKey) EditText appKeyET;
    @InjectView(R.id.et_appSecret) EditText appSecretET;
    @InjectView(R.id.et_backendAppKey) EditText backendAppKeyET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Init SDK");
        return inflater.inflate(R.layout.fragment_init, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        readInitData();
    }

    @Override
    public void onDestroy() {
        ButterKnife.reset(this);
        super.onDestroy();
    }

    private void readInitData() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        String appKey = preferences.getString(Preferences.APP_KEY, Preferences.APP_KEY_DEFAULT);
        String appSecret = preferences.getString(Preferences.APP_SECRET, Preferences.APP_SECRET_DEFAULT);
        String backendAppKey = preferences.getString(Preferences.BACKEND_APP_KEY, Preferences.BACKEND_APP_KEY_DEFAULT);

        appKeyET.setText(appKey);
        appSecretET.setText(appSecret);
        backendAppKeyET.setText(backendAppKey);
    }

    @OnClick(R.id.btn_setInitData)
    public void setAppId() {
        String appKey = appKeyET.getText().toString();
        String appSecret = appSecretET.getText().toString();
        String backendAppKey = backendAppKeyET.getText().toString();
        boolean commitSucceeded = persistInitData(appKey, appSecret, backendAppKey);
        if (commitSucceeded) {
            Timber.d("setting init data succeeded; app key: '%s', app secret: '%s', backend key: '%s'",
                    appKey, appSecret, backendAppKey);
            Application.exit();
        } else {
            Timber.e("setting init data failed; app key: '%s', app secret: '%s', backend key: '%s'",
                    appKey, appSecret, backendAppKey);
            Toast.makeText(this.getActivity(), "saving app id failed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves SDK init data for later use.
     * @return true if the new values were successfully written to persistent storage
     */
    private boolean persistInitData(String appId, String appSecret, String backendAppKey) {
        SharedPreferences preferences = getActivity().getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.APP_KEY, appId);
        editor.putString(Preferences.APP_SECRET, appSecret);
        editor.putString(Preferences.BACKEND_APP_KEY, backendAppKey);
        return editor.commit();
    }
}
