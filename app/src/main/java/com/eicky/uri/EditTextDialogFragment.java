package com.eicky.uri;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.eicky.R;

/**
 * Author: Wei xiao min
 * Email: weixiaomin02@maoyan.com
 * Date: 2018/5/14
 */
// 编辑弹窗
public final class EditTextDialogFragment extends AppCompatDialogFragment {

    private EditText etUriDescription;
    private String uri;
    private Interactor interactor;

    public static EditTextDialogFragment newInstance(@NonNull String uri, @Nullable Interactor interactor) {
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString("uri", uri);
        fragment.setArguments(bundle);
        fragment.interactor = interactor;
        return fragment;
    }

    public static EditTextDialogFragment newInstance(String uri) {
        return newInstance(uri, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        uri = arguments.getString("uri");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_view_edit_uri_description, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etUriDescription = (EditText) view.findViewById(R.id.et_uri_description);
        TextView showUri = (TextView) view.findViewById(R.id.show_uri);
        if (!TextUtils.isEmpty(uri)) {
            showUri.setText(uri);
        }

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (interactor != null) {
                    interactor.onCacelClick(EditTextDialogFragment.this, uri);
                }
            }
        });

        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (interactor != null) {
                    interactor.onOkClick(EditTextDialogFragment.this, uri, etUriDescription.getText().toString().trim());
                }
            }
        });
    }

    public interface Interactor {
        void onCacelClick(EditTextDialogFragment fragment, String uri);

        void onOkClick(EditTextDialogFragment fragment, String uri, String description);
    }
}