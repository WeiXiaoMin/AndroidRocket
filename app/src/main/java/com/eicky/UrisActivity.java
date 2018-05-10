package com.eicky;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class UrisActivity extends AppCompatActivity {
    private static final String TAG = "UrisActivity";

    private AutoCompleteTextView mEtActivityHost;
    private AutoCompleteTextView mEtActivityPath;
    private AutoCompleteTextView mEtActivityQuery;
    private CacheManager<String> mHostCacheManager;
    private CacheManager<String> mPathCacheManager;
    private CacheManager<String> mQueryCacheManager;
    private CacheManager<UriCacheBean> mUriCacheManager;
    private ArrayAdapter<String> mHostAdapter;
    private ArrayAdapter<String> mPathAdapter;
    private ArrayAdapter<String> mQueryAdapter;
    private Toast mToast;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uris);

        mHostCacheManager = new CacheManager<>(this, "uri_scheme_and_host", 20);
        mPathCacheManager = new CacheManager<>(this, "uri_path", 20);
        mQueryCacheManager = new CacheManager<>(this, "uri_query", 20);
        mUriCacheManager = new CacheManager<>(this,"uri_bean", 50);

        mEtActivityHost = (AutoCompleteTextView) findViewById(R.id.et_activity_host);
        mEtActivityPath = (AutoCompleteTextView) findViewById(R.id.et_activity_path);
        mEtActivityQuery = (AutoCompleteTextView) findViewById(R.id.et_activity_query);
        listView = (ListView) findViewById(R.id.listView);

        findViewById(R.id.btn_to_activity_by_uri).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoUri();

            }
        });

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cacheUriBean();
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

    private void cacheUriBean() {
        // TODO-WXM: 2018/5/10 保存uri
    }

    private void gotoUri() {
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

    private static final class CacheManager<T extends Serializable> {
        private final String key;
        private final int limitCount;
        private final ACache aCache;
        private LinkedList<T> list;

        CacheManager(Context context, String key, int limitCount) {
            aCache = ACache.get(context);
            this.key = key;
            this.limitCount = limitCount;
            list = new LinkedList<>();
            if (!TextUtils.isEmpty(key)) {
                Object object = aCache.getAsObject(key);
                if (object != null) {
                    if (object instanceof List) {
                        try {
                            List<T> objs = (List<T>) object;
                            this.list.addAll(objs);
                        } catch (ClassCastException e) {
                            aCache.remove(key);
                        }
                    }
                }
            }
        }

        void add(T t) {
            if (list.contains(t)) {
                if (list.indexOf(t) > 0) {
                    list.remove(t);
                    list.add(0, t);
                }
                return;
            }
            while (list.size() >= limitCount) {
                int index = list.size() - 1;
                list.remove(index);
            }
            list.add(0, t);
        }

        T get(int index) {
            return list.get(index);
        }

        LinkedList<T> getList() {
            return list;
        }

        void executeCache() {
            JSONArray jsonArray = new JSONArray(list);
            aCache.put(key, jsonArray);
        }
    }

    private static final class UriCacheBean implements Serializable{
        String name;
        String uri;
    }
}
