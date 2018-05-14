package com.eicky.uri;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.eicky.ACache;
import com.eicky.R;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private BaseAdapter mUriCacheAdapter;
    private List<HashMap<String, String>> mUriCacheList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uris);

        mHostCacheManager = new CacheManager<>(this, "uri_scheme_and_host", 20);
        mPathCacheManager = new CacheManager<>(this, "uri_path", 20);
        mQueryCacheManager = new CacheManager<>(this, "uri_query", 20);
        mUriCacheManager = new CacheManager<>(this, "uri_bean", 100, getFilesDir());

        mEtActivityHost = (AutoCompleteTextView) findViewById(R.id.et_activity_host);
        mEtActivityPath = (AutoCompleteTextView) findViewById(R.id.et_activity_path);
        mEtActivityQuery = (AutoCompleteTextView) findViewById(R.id.et_activity_query);
        ListView listView = (ListView) findViewById(R.id.listView);

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

        // Uri保存
        mUriCacheAdapter = new BaseAdapter() {

            @Override
            public int getCount() {
                return mUriCacheManager.getList().size();
            }

            @Override
            public Object getItem(int position) {
                return mUriCacheManager.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(UrisActivity.this, android.R.layout.simple_list_item_2, parent);
                }
                TextView tv1 = (TextView) convertView.findViewById(android.R.id.text1);
                TextView tv2 = (TextView) convertView.findViewById(android.R.id.text2);
                UriCacheBean bean = mUriCacheManager.get(position);
                tv1.setText(bean.name);
                tv2.setText(bean.uri);
                return convertView;
            }
        };

        listView.setAdapter(mUriCacheAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UriCacheBean bean = mUriCacheManager.get(position);
                Uri uri = Uri.parse(bean.uri);
                String text1 = uri.getScheme() + uri.getHost();
                String text2 = uri.getPath();
                String text3 = uri.getQuery();
                if (!TextUtils.isEmpty(text1)) {
                    mEtActivityHost.setText(text1);
                }
                if (!TextUtils.isEmpty(text2)) {
                    mEtActivityPath.setText(text1);
                }
                if (!TextUtils.isEmpty(text3)) {
                    mEtActivityQuery.setText(text1);
                }
            }
        });
    }

    private void cacheUriBean() {
        final String uriStr = mEtActivityHost.getText().toString().trim() +
                mEtActivityPath.getText().toString().trim() +
                mEtActivityQuery.getText().toString().trim();
        if (TextUtils.isEmpty(uriStr)) {
            Toast.makeText(this, "色即是空，空即是色", Toast.LENGTH_SHORT).show();
        }

        EditTextDialogFragment fragment = EditTextDialogFragment.newInstance(uriStr, new EditTextDialogFragment.Interactor() {
            @Override
            public void onCacelClick(EditTextDialogFragment fragment, String uri) {
                // 忽略
            }

            @Override
            public void onOkClick(EditTextDialogFragment fragment, String uri, String description) {
                mUriCacheManager.add(new UriCacheBean(description, uri));
                mUriCacheAdapter.notifyDataSetChanged();
            }
        });
        fragment.show(getSupportFragmentManager(), "uri_description_edit_dialog");
    }

    private void gotoUri() {
        final String host = mEtActivityHost.getText().toString().trim();
        final String path = mEtActivityPath.getText().toString().trim();
        final String query = mEtActivityQuery.getText().toString().trim();
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
        mUriCacheManager.executeCache();
    }

    private static final class UriCacheBean implements Serializable {
        String name;
        String uri;

        UriCacheBean(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof UriCacheBean && TextUtils.equals(this.uri, ((UriCacheBean) obj).uri);
        }
    }
}
