package com.eicky.uri;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eicky.R;
import com.eicky.util.GetPathFromUri4kitkat;
import com.eicky.util.PermissionHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class UrisActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_OPEN_FILE = 1;
    public static final int REQUEST_CODE_WRITE_PERMISSION = 1;

    private AutoCompleteTextView mEtActivityHost;
    private AutoCompleteTextView mEtActivityPath;
    private AutoCompleteTextView mEtActivityQuery;
    private AutoCompleteTextView mEtActivityFragment;

    private CacheManager<String> mHostCacheManager;
    private CacheManager<String> mPathCacheManager;
    private CacheManager<String> mQueryCacheManager;
    private CacheManager<UriCacheBean> mUriCacheManager;

    private ArrayAdapter<String> mHostAdapter;
    private ArrayAdapter<String> mPathAdapter;
    private ArrayAdapter<String> mQueryAdapter;
    private Toast mToast;
    private BaseAdapter mUriCacheAdapter;
    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uris);

        mPermissionHelper = new PermissionHelper(this, REQUEST_CODE_WRITE_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        mHostCacheManager = new CacheManager<>(this, "uri_scheme_and_host", 20);
        mPathCacheManager = new CacheManager<>(this, "uri_path", 20);
        mQueryCacheManager = new CacheManager<>(this, "uri_query", 20);
        mUriCacheManager = new CacheManager<>(this, "uri_bean", 100, getFilesDir());

        initView();
    }

    private void initView() {
        mEtActivityHost = (AutoCompleteTextView) findViewById(R.id.et_activity_host);
        mEtActivityPath = (AutoCompleteTextView) findViewById(R.id.et_activity_path);
        mEtActivityQuery = (AutoCompleteTextView) findViewById(R.id.et_activity_query);
        mEtActivityFragment = (AutoCompleteTextView) findViewById(R.id.et_activity_fragment);
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
                showUri(bean.uri);
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

    private void showUri(String uriStr) {
        Uri uri = Uri.parse(uriStr);
        String text1 = uri.getScheme() + "://" + uri.getHost();
        String text2 = uri.getPath() != null ? uri.getPath() : "";
        String text3 = TextUtils.isEmpty(uri.getQuery()) ? "" : "?" + uri.getQuery();
        String text4 = TextUtils.isEmpty(uri.getFragment()) ? "" : "#" + uri.getFragment();
        mEtActivityHost.setText(text1);
        mEtActivityPath.setText(text2);
        mEtActivityQuery.setText(text3);
        if (mEtActivityFragment.getVisibility() == View.VISIBLE) {
            mEtActivityFragment.setText(text4);
        }
    }

    private void cacheUriBean() {
        String fragmentOfUri = "";
        if (mEtActivityFragment.getVisibility() == View.VISIBLE) {
            fragmentOfUri = mEtActivityFragment.getText().toString().trim();
        }
        final String uriStr = mEtActivityHost.getText().toString().trim() +
                mEtActivityPath.getText().toString().trim() +
                mEtActivityQuery.getText().toString().trim() +
                fragmentOfUri;
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
        final String fragment = mEtActivityFragment.getVisibility() == View.VISIBLE ?
                mEtActivityFragment.getText().toString().trim() : "";
        if (TextUtils.isEmpty(host)) {
            showToast("scheme和host不能为空");
            return;
        }
        String uri = host + path + query + fragment;

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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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

        } else if (itemId == R.id.wholeUri) {
            showWholeUriEditDialog();
        } else if (itemId == R.id.outputFile) {
            outputFile(mUriCacheManager.getList());
        } else if (itemId == R.id.setting) {
            showSettingDialog();
        } else if (itemId == R.id.encode_uri) {
            showEncodeDialog();
        } else if (itemId == R.id.decode_uri) {
            showDecodeDialog();
        } else if (itemId == R.id.openCacheDir) {
            openCustomCacheDir();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void showSettingDialog() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        CheckBox cb1 = new CheckBox(this);
        cb1.setText("支持fragment");
        if (mEtActivityFragment.getVisibility() == View.VISIBLE) {
            cb1.setChecked(true);
        } else {
            cb1.setChecked(false);
        }
        cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEtActivityFragment.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        ll.addView(cb1);

        final Button clearBtn = new Button(this);
        clearBtn.setText("清空缓存数据");
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUriCacheManager.clear();
            }
        });
        ll.addView(clearBtn);

        new AlertDialog.Builder(this)
                .setView(ll)
                .setTitle("当前页面设置")
                .setPositiveButton(android.R.string.ok, null)
                .show();

    }

    @Nullable
    private File getCustomCacheDir() {
        File file = Environment.getExternalStorageDirectory();
        if (file != null) {
            file = new File(file, "AndroidRocket");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    private void openCustomCacheDir() {
        final File dir = getCustomCacheDir();
        if (dir == null) {
            showToast("没有外部存储目录，打开失败");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("保存位置：" + dir.getPath())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void outputFile(List<UriCacheBean> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(list);
        File file = getCustomCacheDir();
        if (file == null) {
            showToast("没有外部存储目录，保存失败");
            return;
        }
        String date = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault()).format(new Date());
        String fileName = "uri_" + date;

        final EditText editText = new EditText(this);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setText(fileName);

        final File finalFile = file;
        new AlertDialog.Builder(this)
                .setView(editText)
                .setTitle("输入文件名")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(UrisActivity.this,"文件名不能为空",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        File jsonFile = new File(finalFile, text + ".json");
                        writeJsonFile(json, jsonFile);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void writeJsonFile(String json, File jsonFile) {
        try {
            FileOutputStream fos = new FileOutputStream(jsonFile);
            fos.write(json.getBytes());
            fos.flush();
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("保存成功，保存位置：" + jsonFile.getPath())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showWholeUriEditDialog() {
        final EditText editText = new EditText(this);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setHint("请输入完整的uri");
        new AlertDialog.Builder(this)
                .setView(editText)
                .setTitle("输入完整uri")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(text)) {
                            showUri(text);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEncodeDialog() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(this);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setHint("输入需要编码的内容");
        ll.addView(editText);

        Button button = new Button(this);
        button.setText("编码");
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    String encode = Uri.encode(text);
                    editText.setText(encode);
                }
            }
        });
        ll.addView(button);

        new AlertDialog.Builder(this)
                .setView(ll)
                .setTitle("uri编码")
                .setPositiveButton("确定并复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        String text = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(text) && cm != null) {
                            cm.setPrimaryClip(ClipData.newPlainText(null, text));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDecodeDialog() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(this);
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setHint("输入需要解的内容");
        ll.addView(editText);

        Button button = new Button(this);
        button.setText("解码");
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    String decode = Uri.decode(text);
                    editText.setText(decode);
                }
            }
        });
        ll.addView(button);

        new AlertDialog.Builder(this)
                .setView(ll)
                .setTitle("uri解码")
                .setPositiveButton("确定并复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        String text = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(text) && cm != null) {
                            cm.setPrimaryClip(ClipData.newPlainText(null, text));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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
                    readFile(file);
                }
            }
        }
    }

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
