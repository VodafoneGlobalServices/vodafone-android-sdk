package com.vodafone.global.sdk.testapp.logging.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.vodafone.global.sdk.testapp.R;
import com.vodafone.global.sdk.testapp.logging.database.LogColumns;
import com.vodafone.global.sdk.testapp.logging.database.LogsProvider;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import timber.log.Timber;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogFragment extends Fragment {
    private static final int LOADER_ID = 20;

    @InjectView(android.R.id.list) ListView listView;
    @InjectView(android.R.id.empty) TextView emptyView;

    private LogsAdapter adapter;

    public static LogFragment newInstance() {
        return new LogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.logs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Context appContext = getActivity().getApplicationContext();
        switch (item.getItemId()) {
            case R.id.clear_logs:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContentResolver contentResolver = appContext.getContentResolver();
                        contentResolver.delete(LogsProvider.Logs.LOGS, null, null);
                    }
                }).run();
                return true;
            case R.id.action_send_logs:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendLogsViaEmail(appContext);
                    }
                }).run();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendLogsViaEmail(Context appContext) {
        try {
            List<String[]> records = readLogs(appContext);
            String csv = createCSV(records);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TEXT, csv);
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date());
            intent.putExtra(Intent.EXTRA_SUBJECT, "seamless-id-logs-" + date + ".csv");
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e, "failed sending email");
        }
    }

    private List<String[]> readLogs(Context appContext) {
        ContentResolver contentResolver = appContext.getContentResolver();
        Cursor cursor = contentResolver.query(LogsProvider.Logs.LOGS, null, null, null, null);
        List<String[]> records = new ArrayList<String[]>();
        while (cursor.moveToNext()) {
            String timestamp = cursor.getString(cursor.getColumnIndex(LogColumns.TIMESTAMP));
            String level = cursor.getString(cursor.getColumnIndex(LogColumns.LEVEL));
            String tag = cursor.getString(cursor.getColumnIndex(LogColumns.TAG));
            String msg = cursor.getString(cursor.getColumnIndex(LogColumns.MSG));
            records.add(new String[]{timestamp, level, tag, msg});
        }
        return records;
    }

    private String createCSV(List<String[]> records) throws IOException {
        StringBuilder builder = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withHeader("timestamp", "level", "tag", "msg");
        CSVPrinter printer = new CSVPrinter(builder, format);
        printer.printRecords(records);
        return builder.toString();
    }

    class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), LogsProvider.Logs.LOGS, null, null, null, LogColumns._ID + " DESC");
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
