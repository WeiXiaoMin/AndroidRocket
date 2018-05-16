package com.eicky.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by Wei Xiaomin on 2017/12/30.
 */

public final class PermissionHelper {

    private final Helper mHelper;
    private final String[] mPermissions;
    private final int mRequestCode;
    private final ArrayList<String> mNeverShowRationalePermissions;
    private Callback mCallback;

    public PermissionHelper(Activity activity, int requestCode, String... permissions) {
        this.mHelper = new ActivityHelper(activity);
        this.mPermissions = permissions;
        this.mRequestCode = requestCode;
        mNeverShowRationalePermissions = new ArrayList<>();
    }

    public PermissionHelper(Fragment fragment, int requestCode, String... permissions) {
        this.mHelper = new FragmentHelper(fragment);
        this.mPermissions = permissions;
        this.mRequestCode = requestCode;
        mNeverShowRationalePermissions = new ArrayList<>();
    }

    public void execute(Callback callback) {
        this.mCallback = callback;
        if (checkPermissions()) {
            if (mCallback != null) {
                mCallback.result(true);
            }
        } else {
            mHelper.requestPermissions(mPermissions, mRequestCode);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean granted = true;
        for (String pms : mPermissions) {
            if (!mHelper.checkSelfPermission(pms)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != mRequestCode) {
            return;
        }
        onRequestPermissionsResult(permissions, grantResults);
    }

    public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isGranted = true;
        mNeverShowRationalePermissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                if (!mHelper.shouldShowRequestPermissionRationale(permission)) {
                    // 不再询问
                    mNeverShowRationalePermissions.add(permission);
                }
            }
        }
        if (isGranted) {
            if (mCallback != null) {
                mCallback.result(true);
            }
        } else if (mNeverShowRationalePermissions.size() > 0) {
            String[] pmss = new String[mNeverShowRationalePermissions.size()];
            mNeverShowRationalePermissions.toArray(pmss);
            if (mCallback != null) {
                mCallback.onNeverShowRationale(pmss);
            }
        } else {
            if (mCallback != null) {
                mCallback.result(false);
            }
        }
    }

    interface Helper {
        boolean checkSelfPermission(String permission);

        boolean shouldShowRequestPermissionRationale(String permission);

        void requestPermissions(String[] permissions, int requestCode);
    }

    static final class ActivityHelper implements Helper {
        private Activity activity;

        ActivityHelper(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean checkSelfPermission(String permission) {
            return ActivityCompat.checkSelfPermission(this.activity, permission) == PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(String permission) {
            return ActivityCompat.shouldShowRequestPermissionRationale(this.activity, permission);
        }

        @Override
        public void requestPermissions(String[] permissions, int requestCode) {
            ActivityCompat.requestPermissions(this.activity, permissions, requestCode);
        }
    }

    static final class FragmentHelper implements Helper {

        private Fragment fragment;

        FragmentHelper(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public boolean checkSelfPermission(String permission) {
            return ContextCompat.checkSelfPermission(this.fragment.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(String permission) {
            return this.fragment.shouldShowRequestPermissionRationale(permission);
        }

        @Override
        public void requestPermissions(String[] permissions, int requestCode) {
            this.fragment.requestPermissions(permissions, requestCode);
        }
    }

    public interface Callback {
        void result(boolean granted);

        void onNeverShowRationale(String[] permissions);
    }

    public static abstract class DialogTipsCallback implements Callback {
        private Context context;

        protected DialogTipsCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onNeverShowRationale(String[] permissions) {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage(getTips(permissions))
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DialogTipsCallback.this.onNegativeButtonClick(dialog, which);
                        }
                    })
                    .show();
        }

        protected void onNegativeButtonClick(DialogInterface dialog, int which) {
        }

        public abstract String getTips(String[] permissions);
    }
}
