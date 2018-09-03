package com.example.administrator.dabaggo;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import com.kakao.sdk.newtoneapi.SpeechRecognizerActivity;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // Added by NDJ <start> 18.08.27
    String clientId = ""; // papago open api 사용을 위한 client id
    String clientSecret = ""; // papago open api 사용을 위한 client secret key
    String css_clientId = ""; // 음성 번역을 위한 api 사용을 위한 client id
    String css_clientSecret = ""; // 음성 번역을 위한 api 사용을 위한 client secret key
    List <String> languages;
    List <String> keywords;
    List <LangVO> active_list;

    int charCount = 0; // CSS API 몇 자를 사용했는지 check
    final int limitMAX = 1000; // CSS API 사용 제한 글자수

    Handler handler = new Handler();
    int fromIndex = 0; // 시작 인덱스[한국어]
    ArrayAdapter langAdapter = null;
    CustomAdapter contentAdapter = null;
    ListView listView;
    // Added by NDJ <end> 18.08.27

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK: // kakao 음성 api로부터 데이터를 성공적으로 얻어오는 경우
                ArrayList<String> results = data.getStringArrayListExtra(VoiceRecoActivity.EXTRA_KEY_RESULT_ARRAY);
                final String ans = results.get(0);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EditText txtquestion = (EditText) findViewById(R.id.txtquestion);
                        txtquestion.setText(ans);
                    }
                });
                break;
            case RESULT_CANCELED: // kakao 음성 api로부터 데이터 얻어오기 실패
                if (data == null) break;
                // 에러 발생 시 처리 코드
                int errorCode = data.getIntExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_CODE, -1);
//                String errorMsg = data.getStringExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_MESSAGE);
                String errorMsg = getResources().getString(R.string.text_get_fail);

                if (errorCode != -1 && !TextUtils.isEmpty(errorMsg)) {
                    new AlertDialog.Builder(this).
                            setMessage(errorMsg).
                            setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).
                            show();
                }
                break;
            case 1: // Setting Activity
                if (data != null && data.getSerializableExtra("lang") != null) {
                    ArrayList<Integer> r = (ArrayList<Integer>) data.getSerializableExtra("lang");
                    active_list.clear(); // initialize list.
                    for (int i = 0; i < r.size(); i++) {
                        int idx = r.get(i);
                        LangVO item = new LangVO(idx,languages.get(idx),"",true);
                        active_list.add(item);
                    }
                }
                setListView();
                saveFile();
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
        clientId = getResources().getString(R.string.papago_client_id);
        clientSecret = getResources().getString(R.string.papago_client_secret);
        css_clientId = getResources().getString(R.string.naver_client_id);
        css_clientSecret = getResources().getString(R.string.naver_client_secret);

        EditText txtquestion = findViewById(R.id.txtquestion);
        Button btnTrans = findViewById(R.id.btn_search);
        Button btnRecord = findViewById(R.id.btn_record);
        listView = findViewById(R.id.list_view);

        // array.xml에서 string list로 가져옴.
        languages = Arrays.asList(getResources().getStringArray(R.array.language));
        keywords = Arrays.asList(getResources().getStringArray(R.array.keyword));

        // list view 설정
        active_list = new ArrayList<>();

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

        // Added by NDJ <start> 18.08.29 카카오 음성 인식 기능 추가
        btnRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), VoiceRecoActivity.class);
                String serviceType = SpeechRecognizerClient.SERVICE_TYPE_WEB;
                i.putExtra(SpeechRecognizerActivity.EXTRA_KEY_SERVICE_TYPE, serviceType);
                startActivityForResult(i, 0);
            }
        });
        // Added by NDJ <end> 18.08.29

        // setiing.bin file 읽기

        String filePath = getApplicationContext().getFilesDir().getPath().toString()  + "/setting.bin";
        File set_file = new File(filePath);

        if (set_file.exists()) {
            readFile();
        }
        else {
            // 기본적으로 영어, 일본어, 중국어 간체를 setting
            for (int i = 1; i < 4; i++) {
                LangVO item = new LangVO(i,languages.get(i),"",true);
                active_list.add(item);
            }
        }
        // list view 설정
        setListView();
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

                ArrayList<Integer> results = new ArrayList<>();
                for (int i = 0; i < active_list.size(); i++) {
                    results.add(active_list.get(i).index);
                }
                intent.putExtra("lang", results);

                startActivityForResult(intent, 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTranslate(String sourceText) {
        final String target = sourceText;
        final String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

        // papago api와 통신하기 위한 쓰레드 처리.
        ExecutorService exeService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < active_list.size(); i++) {
            final int idx = i;
            exeService.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        final String text = URLEncoder.encode(target,"UTF-8");

                        LangVO item = active_list.get(idx);
                        int from = fromIndex;
                        int to = item.index;

                        // 참조 언어와 타겟 언어가 동일한 경우.
                        if (from == to) {
                            active_list.get(idx).content = target;
                        }
                        else {
                            if (item.isChecked) {

                                try {
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

                                    Log.e("response",response.toString());

                                    // 3. get json data
                                    JSONObject result_obj = new JSONObject(response.toString());

                                    if (result_obj.has("message")) {
                                        JSONObject msg_obj = result_obj.getJSONObject("message");
                                        JSONObject res_obj = msg_obj.getJSONObject("result");
                                        final String translated = res_obj.getString("translatedText");

                                        // 4. print the translated text
                                        active_list.get(idx).content = translated;

                                        Log.e("from",from + " ");
                                        Log.e("to",to + " ");
                                        Log.e("text",translated);
                                    }
                                    else {
                                        // 4. print the translated text
                                        active_list.get(idx).content = result_obj.getString("errorMessage") + "/" + languages.get(from) + "->" + languages.get(to);
                                    }

                                }
                                catch (Exception e) {
                                    // 4. print the translated text
                                    active_list.get(idx).content = "Not Supported Type from " + languages.get(from) + " to " + languages.get(to);
                                }
                            }
                            else {
                                active_list.get(idx).content = "";
                            }
                        }

                    } catch (Exception e) {
                        Log.e("Exception","Exception happened.");
                    }
                }
            });
        }
        exeService.shutdown();
        while (!exeService.isTerminated()) {
        }
        // 위에까지가 쓰레드가 모두 처리되길 기다리는 과정.
        Log.e("Process","Finished");

        // start by SMH, To make widget process
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.administrator.dabaggo.sharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("txt_lang");
        editor.putString("txt_lang", sourceText);
        editor.remove("status_size");
        editor.putInt("status_size", active_list.size());

        for (int i = 0; i < active_list.size(); i++) {
            editor.remove("status_lang_" + i);
            editor.remove("status_cnt_" + i);

            editor.putString("status_lang_" + i, active_list.get(i).lang);

            editor.putString("status_cnt_" + i, active_list.get(i).content);

        }

        for (int i = 0; i < active_list.size(); i++) {
            Log.i("LangContent :: ", active_list.get(i).lang + "" + active_list.get(i).content);
        }

        editor.apply();
        editor.commit();

        Intent intent = new Intent(MainActivity.this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("data", "ddddd");
        MainActivity.this.sendBroadcast(intent);
        // end by SMH To make widget process
    }

    // setting.bin 저장 by NDJ 180831
    public void saveFile() {
        OutputStream out = null;
        BufferedOutputStream bout = null;
        ObjectOutputStream oout = null;

        try {

            out = new FileOutputStream(getApplicationContext().getFilesDir().getPath().toString()  + "/setting.bin");
            bout = new BufferedOutputStream(out);
            oout = new ObjectOutputStream(bout);

            ArrayList<Integer> int_list = new ArrayList<>();

            for (int i = 0; i < active_list.size(); i++) {
                int_list.add(active_list.get(i).index);
            }

            oout.writeObject(int_list);
            oout.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // setting.bin 불러오기 by NDJ 180831
    public void readFile() {
        InputStream in = null;
        BufferedInputStream bin = null;
        ObjectInputStream oin = null;

        ArrayList<Integer> int_list = new ArrayList<>();

        try {
            in = new FileInputStream(getApplicationContext().getFilesDir().getPath().toString()  + "/setting.bin");
            bin = new BufferedInputStream(in);
            oin = new ObjectInputStream(bin);

            int_list = (ArrayList<Integer>) oin.readObject();
            active_list.clear(); // initialize list.

            for (int i = 0; i < int_list.size(); i++) {
                int idx = int_list.get(i);
                LangVO item = new LangVO(idx,languages.get(idx),"",true);
                active_list.add(item);
            }
            oin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    // list view 초기화
    public void setListView() {
        if (active_list != null && active_list.size() > 0) {
            // listview ui 초기화 part
            contentAdapter = new CustomAdapter(this, R.layout.text_item, active_list);
            listView.setAdapter(contentAdapter);
        }
    }

}
