package com.eicky;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class OtherFunctionsActivity extends AppCompatActivity {

    private AppCompatEditText mEtActivityUri;
    private AppCompatButton mBtnToActivityByUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_functions);

        mEtActivityUri = (AppCompatEditText) findViewById(R.id.et_activity_uri);
        mBtnToActivityByUri = (AppCompatButton) findViewById(R.id.btn_to_activity_by_uri);

        mBtnToActivityByUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Editable text = mEtActivityUri.getText();
                if (text != null && !TextUtils.isEmpty(text.toString())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(text.toString()));
                    startActivity(intent);
                } else {
                    Toast.makeText(OtherFunctionsActivity.this, "uri不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
