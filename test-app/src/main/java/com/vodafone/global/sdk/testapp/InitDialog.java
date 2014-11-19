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
        getDialog().setTitle("Edit init data");
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

    @OnClick(R.id.btn_save)
    public void save() {
        Timber.v("init dialog: save clicked");
        persistInitData();
    }

    @OnClick(R.id.btn_save_and_reset)
    public void saveAndReset() {
        Timber.v("init dialog: save clicked");
        persistInitData();
        Application.exit();
    }

    /**
     * Saves SDK init data for later use.
     */
    private void persistInitData() {
        String appKey = appKeyET.getText().toString();
        String appSecret = appSecretET.getText().toString();
        String backendAppKey = backendAppKeyET.getText().toString();

        SharedPreferences preferences = getActivity().getSharedPreferences(Preferences.DEFAULT_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.APP_KEY, appKey);
        editor.putString(Preferences.APP_SECRET, appSecret);
        editor.putString(Preferences.BACKEND_APP_KEY, backendAppKey);
        boolean commitSucceeded = editor.commit();

        if (commitSucceeded) {
            Timber.v("saving init data; app key: '%s', app secret: '%s', backend key: '%s'",
                    appKey, appSecret, backendAppKey);
        } else {
            Timber.e("saving init data failed");
            Toast.makeText(this.getActivity(), "saving app id failed", Toast.LENGTH_LONG).show();
        }
    }
}
