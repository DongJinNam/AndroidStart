package com.example.administrator.mypapagoapp;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

    final String clientId = "NAobSAafX516i7HN0sKs";
    final String clientSecret = "sQIGQOACoF";
    List <String> languages;
    List <String> keywords;
    List <LangVO> active_list;

    Handler handler = new Handler();
    int fromIndex = 0; // 시작 인덱스[한국어]
    int toIndex = 1; // 도착 인덱스[영어]
    ArrayAdapter langAdapter = null;
    ArrayAdapter langAdapter2 = null;
    Spinner langSpinner = null;
    Spinner langSpinner2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText txtquestion = findViewById(R.id.txtquestion);
        Button btnTrans = findViewById(R.id.btnTrans);
        ListView listView = findViewById(R.id.list_view);

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

        langAdapter2 = ArrayAdapter.createFromResource(this,R.array.language,android.R.layout.simple_spinner_dropdown_item);
        langSpinner2 = findViewById(R.id.sp_to_type);
        langSpinner2.setAdapter(langAdapter2);
        langSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = getString(R.string.target_lan) + (String) langAdapter2.getItem(position);
                toIndex = position;
                Toast.makeText(MainActivity.this, str,Toast.LENGTH_SHORT).show();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isChecked = !active_list.get(toIndex).isChecked;
                        active_list.get(toIndex).isChecked = isChecked;
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 기본 영어로 선택.
        langSpinner2.setSelection(toIndex,false);
        toIndex = 1;
        
        // list view 설정
        active_list = new ArrayList<>();
        for (int i = 0; i < languages.size(); i++) {
            if (i == 1)
                active_list.add(new LangVO(i,languages.get(i),"",true));
            else
                active_list.add(new LangVO(i,languages.get(i),"",false));
        }
        CustomAdapter adapter2 = new CustomAdapter(this, R.layout.text_item, active_list);
        listView.setAdapter(adapter2);        
        

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
    }

    public void startTranslate(String sourceText) {
        final String target = sourceText;

        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. set connect with papago
//                            String text = URLEncoder.encode("만나서 반갑습니다.", "UTF-8");
                    String text = URLEncoder.encode(target,"UTF-8");
                    String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                    for (int i = 0; i < active_list.size(); i++) {
                        int from = fromIndex;
                        int to = i;

                        if (active_list.get(i).isChecked) {
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
                            active_list.get(to).content = translated;

                            Log.e("from",from + " ");
                            Log.e("to",to + " ");
                            Log.e("text",translated);
                        }
                        else {
                            active_list.get(to).content = "";
                        }
                    }

                } catch (Exception e) {

                    // 4. print the translated text
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            active_list.get(to).content = getString(R.string.not_support) + ", " + languages.get(from) + " to " + languages.get(to);
//                        }
//                    });

                    System.out.println(e);
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
