package com.eicky;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;

public class OtherFunctionsActivity extends AppCompatActivity {
    private static final String TAG = "OtherFunctionsActivity";

    private AutoCompleteTextView mEtActivityHost;
    private AutoCompleteTextView mEtActivityPath;
    private AutoCompleteTextView mEtActivityQuery;
    private CacheManager mHostCacheManager;
    private CacheManager mPathCacheManager;
    private CacheManager mQueryCacheManager;
    private ArrayAdapter<String> mHostAdapter;
    private ArrayAdapter<String> mPathAdapter;
    private ArrayAdapter<String> mQueryAdapter;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_functions);

        mHostCacheManager = new CacheManager(this, "uri_scheme_and_host", 20);
        mPathCacheManager = new CacheManager(this, "uri_path", 20);
        mQueryCacheManager = new CacheManager(this, "uri_query", 20);

        mEtActivityHost = (AutoCompleteTextView) findViewById(R.id.et_activity_host);
        mEtActivityPath = (AutoCompleteTextView) findViewById(R.id.et_activity_path);
        mEtActivityQuery = (AutoCompleteTextView) findViewById(R.id.et_activity_query);

        AppCompatButton btnToActivityByUri = (AppCompatButton) findViewById(R.id.btn_to_activity_by_uri);

        btnToActivityByUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String host = mEtActivityHost.getText().toString();
                final String path = mEtActivityPath.getText().toString();
                final String query = mEtActivityQuery.getText().toString();
                if (TextUtils.isEmpty(host)) {
                    showToast("scheme和host不能为空");
                    return;
                }
                String uri = host + path + query;

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);

                    mHostCacheManager.add(host);
                    mHostAdapter.clear();
                    mHostAdapter.addAll(mHostCacheManager.getList());
                    if (!TextUtils.isEmpty(path)) {
                        mPathCacheManager.add(path);
                        mPathAdapter.clear();
                        mPathAdapter.addAll(mPathCacheManager.getList());
                    }
                    if (!TextUtils.isEmpty(query)) {
                        mQueryCacheManager.add(query);
                        mQueryAdapter.clear();
                        mQueryAdapter.addAll(mQueryCacheManager.getList());
                    }
                } catch (Exception e) {
                    showToast("请检查输入是否正确");
                }

            }
        });

        mHostAdapter = new ArrayAdapter<String>(this,
                R.layout.textview_arrayitem_uri,
                mHostCacheManager.getList());
        mEtActivityHost.setAdapter(mHostAdapter);

        mPathAdapter = new ArrayAdapter<String>(this,
                R.layout.textview_arrayitem_uri,
                mPathCacheManager.getList());
        mEtActivityPath.setAdapter(mPathAdapter);

        mQueryAdapter = new ArrayAdapter<String>(this,
                R.layout.textview_arrayitem_uri,
                mQueryCacheManager.getList());
        mEtActivityQuery.setAdapter(mQueryAdapter);
    }

    public void showToast(CharSequence text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.cancel();
            mToast.setText(text);
            mToast.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHostCacheManager.executeCache();
        mPathCacheManager.executeCache();
        mQueryCacheManager.executeCache();
    }

    private static final class CacheManager {
        private final String key;
        private final int limitCount;
        private final ACache aCache;
        private LinkedList<String> list;

        CacheManager(Context context, String key, int limitCount) {
            aCache = ACache.get(context);
            this.key = key;
            this.limitCount = limitCount;
            JSONArray jsonArray = aCache.getAsJSONArray(key);

            list = new LinkedList<>();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String str = null;
                    try {
                        str = jsonArray.getString(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (str != null) {
                        list.add(str);
                    }
                }
            }
        }

        void add(String uri) {
            if (list.contains(uri) && list.indexOf(uri) > 0) {
                list.remove(uri);
                list.add(0, uri);
                return;
            }
            while (list.size() >= limitCount) {
                int index = list.size() - 1;
                list.remove(index);
            }
            list.add(0, uri);
        }

        String get(int index) {
            return list.get(index);
        }

        LinkedList<String> getList() {
            return list;
        }

        void executeCache() {
            JSONArray jsonArray = new JSONArray(list);
            aCache.put(key, jsonArray);
        }
    }
}
