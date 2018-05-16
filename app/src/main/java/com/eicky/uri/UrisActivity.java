package com.eicky.uri;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eicky.R;
import com.eicky.util.GetPathFromUri4kitkat;
import com.eicky.util.PermissionHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class UrisActivity extends AppCompatActivity {
    public static final String KEY_DIR_OF_LAST_FILE = "dir_of_last_file";
    public static final int REQUEST_CODE_OPEN_FILE = 1;
    public static final int REQUEST_CODE_READ_PERMISSION = 1;

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
    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uris);

        mPermissionHelper = new PermissionHelper(this, REQUEST_CODE_READ_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE);

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
                    convertView = View.inflate(UrisActivity.this, android.R.layout.simple_list_item_2, null);
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
                String text1 = uri.getScheme() + "://" + uri.getHost();
                String text2 = uri.getPath();
                String text3 = "?" + uri.getQuery();
                if (!TextUtils.isEmpty(text1)) {
                    mEtActivityHost.setText(text1);
                }
                if (!TextUtils.isEmpty(text2)) {
                    mEtActivityPath.setText(text2);
                }
                if (!TextUtils.isEmpty(text3)) {
                    mEtActivityQuery.setText(text3);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(UrisActivity.this)
                        .setTitle("删除数据")
                        .setMessage("是否删除该条数据？")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUriCacheManager.remove(position);
                                mUriCacheAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
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

    private void openFile() {
        // TODO-WXM: 2018/5/16 适配Android 7.0
//        SharedPreferences sp = getSharedPreferences(Config.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
//        String dirOfLastFile = sp.getString(KEY_DIR_OF_LAST_FILE, "");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.parse(dirOfLastFile), "text/*");
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
    }

    private void readFile(File file) {
        try {
            StringBuilder sb = new StringBuilder();
            FileReader reader = new FileReader(file);
            char[] cs = new char[1024];
            int len = 0;
            while ((len = reader.read(cs)) != -1) {
                sb.append(cs, 0, len);
            }
            String json = sb.toString();
            int dataLenght = 0;
            if (!TextUtils.isEmpty(json)) {
                List<UriCacheBean> list = new Gson().fromJson(json, new TypeToken<List<UriCacheBean>>() {
                }.getType());
                if (list != null && !list.isEmpty()) {
                    dataLenght = list.size();
                    mUriCacheManager.addAll(list);
                    mUriCacheAdapter.notifyDataSetChanged();
                }
            }
            Toast.makeText(this,
                    String.format(Locale.getDefault(), "读取完毕,读取到%d条数据", dataLenght),
                    Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_uri, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.readFile) {
            mPermissionHelper.execute(new PermissionHelper.DialogTipsCallback(this) {
                @Override
                public String getTips(String[] permissions) {
                    return "需要授予读取文件的权限";
                }

                @Override
                public void result(boolean granted) {
                    if (granted) {
                        openFile();
                    } else {
                        Toast.makeText(UrisActivity.this, "取消操作", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_OPEN_FILE == requestCode && Activity.RESULT_OK == resultCode) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = GetPathFromUri4kitkat.getPath(this, uri);
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
//                    cacheLastOpenFile(file);
                    readFile(file);
                }
            }
        }
    }

//    private void cacheLastOpenFile(File file) {
//        File dir;
//        if (file.isDirectory()) {
//            dir = file;
//        } else {
//            dir = file.getParentFile();
//        }
//        SharedPreferences sp = getSharedPreferences(Config.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
//        sp.edit().putString(KEY_DIR_OF_LAST_FILE, Uri.fromFile(dir).toString()).apply();
//    }

    private void showToast(CharSequence text) {
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

    public static final class UriCacheBean implements Serializable {
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
