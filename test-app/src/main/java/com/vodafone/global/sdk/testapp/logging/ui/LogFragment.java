package com.vodafone.global.sdk.testapp.logging.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.vodafone.global.sdk.testapp.R;
import com.vodafone.global.sdk.testapp.logging.database.LogsProvider;

public class LogFragment extends Fragment {
    private static final int LOADER_ID = 20;

    @InjectView(android.R.id.list) ListView listView;
    @InjectView(android.R.id.empty) TextView emptyView;

    private LogsAdapter adapter;

    public static LogFragment newInstance() {
        return new LogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        listView.setEmptyView(emptyView);
        if (adapter != null) listView.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_ID, null, new LoaderCallback());
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), LogsProvider.Logs.LOGS, null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (adapter == null) {
                adapter = new LogsAdapter(getActivity(), data);
                listView.setAdapter(adapter);
            } else {
                adapter.changeCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (adapter != null) {
                adapter.changeCursor(null);
            }
        }
    }
}
