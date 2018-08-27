package com.example.administrator.dabaggo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Added by NDJ <start> 18.08.27
    final String clientId = "NAobSAafX516i7HN0sKs";
    final String clientSecret = "sQIGQOACoF";
    List <String> languages;
    List <String> keywords;
    List <LangVO> active_list;

    Handler handler = new Handler();
    int fromIndex = 0; // 시작 인덱스[한국어]
    ArrayAdapter langAdapter = null;
    CustomAdapter contentAdapter = null;
    Spinner langSpinner = null;
    ListView listView;
    // Added by NDJ <end> 18.08.27

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 0:
                ArrayList<Integer> r = (ArrayList<Integer>) data.getSerializableExtra("lang");
                active_list.clear(); // initialize list.
                for (int i = 0; i < r.size(); i++) {
                    int idx = r.get(i);
                    LangVO item = new LangVO(idx,languages.get(idx),"",true);
                    active_list.add(item);
                }
                // listview ui 초기화 part
                contentAdapter = new CustomAdapter(this, R.layout.text_item, active_list);
                listView.setAdapter(contentAdapter);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //        getSupportActionBar().setTitle("dabaggo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_bar);
        getSupportActionBar().setElevation(0);

        // Added by NDJ <start> 18.08.27
        EditText txtquestion = findViewById(R.id.txtquestion);
        Button btnTrans = findViewById(R.id.btnTrans);
        listView = findViewById(R.id.list_view);

        // Open API 경고 문구.
        Toast.makeText(this, getString(R.string.caution), Toast.LENGTH_SHORT).show();

        // array.xml에서 string list로 가져옴.
        languages = Arrays.asList(getResources().getStringArray(R.array.language));
        keywords = Arrays.asList(getResources().getStringArray(R.array.keyword));

        langAdapter = ArrayAdapter.createFromResource(this,R.array.language,android.R.layout.simple_spinner_dropdown_item);
        langSpinner = findViewById(R.id.sp_from_type);
        langSpinner.setAdapter(langAdapter);
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = getString(R.string.source_lan) + (String) langAdapter.getItem(position);
                fromIndex = position;
                Toast.makeText(MainActivity.this, str,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // list view 설정
        active_list = new ArrayList<>();
        contentAdapter = new CustomAdapter(this, R.layout.text_item, active_list);
        listView.setAdapter(contentAdapter);


        btnTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = findViewById(R.id.txtquestion);
                final String target = tv.getText().toString();

                startTranslate(tv.getText().toString());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ListView listView = findViewById(R.id.list_view);
                        CustomAdapter adapter2 = new CustomAdapter(MainActivity.this, R.layout.text_item, active_list);
                        listView.setAdapter(adapter2);
                    }
                });
            }
        });
        // Added by NDJ <end> 18.08.27
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(intent, 0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTranslate(String sourceText) {
        final String target = sourceText;

        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. set connect with papago
                    String text = URLEncoder.encode(target,"UTF-8");
                    String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                    for (int i = 0; i < active_list.size(); i++) {
                        LangVO item = active_list.get(i);
                        int from = fromIndex;
                        int to = item.index;

                        if (item.isChecked) {
                            URL url = new URL(apiURL);
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("X-Naver-Client-Id", clientId);
                            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                            // 2. post request
                            String postParams = "source=";
                            postParams += keywords.get(from);
                            postParams += "&target=";
                            postParams += keywords.get(to);
                            postParams += "&text=";
                            postParams += text;

                            //String postParams = "source=ko&target=en&text=" + text;
                            con.setDoOutput(true);
                            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                            wr.writeBytes(postParams);
                            wr.flush();
                            wr.close();
                            int responseCode = con.getResponseCode();
                            BufferedReader br;
                            if(responseCode==200) { // 정상 호출
                                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            } else {  // 에러 발생
                                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                            }
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = br.readLine()) != null) {
                                response.append(inputLine);
                            }
                            br.close();

                            // 3. get json data
                            JSONObject msg_obj = new JSONObject(response.toString()).getJSONObject("message");
                            JSONObject res_obj = msg_obj.getJSONObject("result");
                            final String translated = res_obj.getString("translatedText");

                            // 4. print the translated text
                            active_list.get(i).content = translated;

                            Log.e("from",from + " ");
                            Log.e("to",to + " ");
                            Log.e("text",translated);
                        }
                        else {
                            active_list.get(i).content = "";
                        }
                    }

                } catch (Exception e) {
                    Log.e("Exception","Exception happened.");
                }
            }
        });
        thr.start();

        try {
            thr.join();
        } catch (InterruptedException e) {
            Log.e("Interrupt","Interrupt Exception happend.");
        }
    }
}
