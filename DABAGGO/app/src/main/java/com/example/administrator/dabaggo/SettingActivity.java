package com.example.administrator.dabaggo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static final String TAG = "SettingActivity";
    Button mBtnAdd;
    private ListView mList;
    private String[] data = {"영어(en)", "중국어(zh)", "스페인어(es)", "프랑스어(fr)", "베트남어(vi)", "태국어(th)", "인도네시아어(id)", "일본어(ja)"};
    List <String> languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Added by NDJ <start> 18.08.27
        languages = Arrays.asList(getResources().getStringArray(R.array.language));
        // Added by NDJ <end> 18.08.27

        mBtnAdd = findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(this);

        mList = findViewById(R.id.list_view);
        mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, languages);

        mList.setAdapter(adapter);
        getSupportActionBar().setElevation(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                SparseBooleanArray checkedArr = mList.getCheckedItemPositions();
                ArrayList<Integer> results = new ArrayList<>();

                for (int i = 0; i < mList.getAdapter().getCount(); i++) {
                    if (checkedArr.get(i) && String.valueOf(checkedArr.get(i)).equals("true")) {
                        // Do something
                        results.add(i);
                    }
                }

                Intent intent = new Intent();
                intent.putExtra("lang", results);
                setResult(0, intent);
                finish();
                break;

            default:
                break;
        }
    }
}
