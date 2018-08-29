package com.example.administrator.dabaggo;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CustomAdapter extends BaseAdapter {
    MainActivity ma;
    int layout;
    List<LangVO> lang_list;
    List<String> voice_man;
    String clientId = "";
    String clientSecret = "";

    public CustomAdapter(MainActivity ma, int layout, List<LangVO> lang_list) {
        this.ma = ma;
        this.layout = layout;
        this.lang_list = lang_list;
        voice_man = Arrays.asList(ma.getResources().getStringArray(R.array.voice_man));
        clientId = ma.css_clientId;
        clientSecret = ma.css_clientSecret;
    }

    // 보여줄 모습
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ma.getLayoutInflater(); // xml 파일을 view로 확장시킨다는 개념.
        View v = inflater.inflate(layout,null);
        TextView tv_output = (TextView) v.findViewById(R.id.tv_output);
        Button btn_play = (Button) v.findViewById(R.id.btn_play);
        ScrollView sv_output = (ScrollView) v.findViewById(R.id.sv_output);
        TextView txtanswer = (TextView) v.findViewById(R.id.txtanswer);

        if (ma.active_list.get(position).isChecked) {
            LangVO obj = ma.active_list.get(position);
            final int obj_index = obj.index;
            final String source = lang_list.get(position).content;

            tv_output.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.VISIBLE);
            sv_output.setVisibility(View.VISIBLE);
            tv_output.setText(lang_list.get(position).lang);
            txtanswer.setText(lang_list.get(position).content);

            btn_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMedia(source,obj_index);
                }
            });
        }
        else {
            tv_output.setVisibility(View.GONE);
            btn_play.setVisibility(View.GONE);
            sv_output.setVisibility(View.GONE);
            tv_output.setText("");
            txtanswer.setText("");
        }

        return v;
    }

    // 보여줄 개수
    @Override
    public int getCount() {
        return lang_list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // target : source text, index : 어떤 언어인지 알려주는 index
    public void startMedia(String target, int index) {
        final String sourceText = target;
        final int voice_index = index;
        // 한국어, 영어, 일본어, 중국어만 음성 지원하도록 설정.
        if (index < 4 && ma.charCount < ma.limitMAX) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String text = URLEncoder.encode(sourceText, "UTF-8"); // 13자
                        String apiURL = "https://naveropenapi.apigw.ntruss.com/voice/v1/tts";
                        URL url = new URL(apiURL);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
                        con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
                        ma.charCount += sourceText.length();

                        // post request
                        String postParams = "speaker=";
                        postParams += voice_man.get(voice_index);
                        postParams += "&speed=0&text=";
                        postParams += text;

                        con.setDoOutput(true);
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(postParams);
                        wr.flush();
                        wr.close();
                        int responseCode = con.getResponseCode();
                        BufferedReader br;
                        if (responseCode == 200) { // 정상 호출
                            InputStream is = con.getInputStream();
                            int read = 0;
                            byte[] bytes = new byte[1024];
                            // 랜덤한 이름으로 mp3 파일 생성
                            String tempname = Long.valueOf(new Date().getTime()).toString();
                            //java.io.File f = new File(tempname + ".mp3");
                            java.io.File f = new File(ma.getApplicationContext().getFilesDir().getPath().toString()  + "/" + tempname + ".mp3");
                            f.setReadable(true);
                            f.setWritable(true);
                            f.setExecutable(true);
                            f.createNewFile();
                            OutputStream outputStream = new FileOutputStream(f);
                            while ((read = is.read(bytes)) != -1) {
                                outputStream.write(bytes, 0, read);
                            }
                            is.close();

                            // 음성 파일을 사이트로부터 얻어와서 실행.
                            MediaPlayer player = new MediaPlayer();

                            FileInputStream myVoice = new FileInputStream(f);
                            player.setDataSource(myVoice.getFD());

                            player.prepare();
                            player.start();

                            Log.e("media","success");

                        } else {  // 에러 발생
                            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = br.readLine()) != null) {
                                response.append(inputLine);
                            }
                            br.close();
                            Log.e("media",response.toString());
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }).start();

        }
        else {
            String text = ma.getResources().getString(R.string.access_deny);
            Toast.makeText(ma.getApplicationContext(),text,Toast.LENGTH_SHORT).show();
        }
    }

}
