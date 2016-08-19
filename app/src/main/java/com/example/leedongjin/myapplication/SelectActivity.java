package com.example.leedongjin.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by LeeDongJin on 2016-08-01.
 */
public class SelectActivity extends Activity{

    TextView nametext;
    EditText input_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);
        nametext = (TextView) findViewById(R.id.name_text);
        input_edit = (EditText) findViewById(R.id.text_input);

        Intent intent = getIntent();
        String product = intent.getStringExtra("name"); //리스트 목록을 클릭했을 때 리스트 제목을 받아옴
        nametext.setText(product);

    }
}
