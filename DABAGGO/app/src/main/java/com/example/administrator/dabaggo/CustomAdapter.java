package com.example.administrator.dabaggo;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomAdapter extends BaseAdapter {


    MainActivity ma;
    int layout;
    List<LangVO> lang_list;


    public CustomAdapter(MainActivity ma, int layout, List<LangVO> lang_list) {
        this.ma = ma;
        this.layout = layout;
        this.lang_list = lang_list;
    }

    // 보여줄 모습
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ma.getLayoutInflater(); // xml 파일을 view로 확장시킨다는 개념.
        View v = inflater.inflate(layout,null);
        TextView tv_output = (TextView) v.findViewById(R.id.tv_output);
        ScrollView sv_output = (ScrollView) v.findViewById(R.id.sv_output);
        TextView txtanswer = (TextView) v.findViewById(R.id.txtanswer);

        if (ma.active_list.get(position).isChecked) {
            tv_output.setVisibility(View.VISIBLE);
            sv_output.setVisibility(View.VISIBLE);
            tv_output.setText(lang_list.get(position).lang);
            txtanswer.setText(lang_list.get(position).content);
        }
        else {
            tv_output.setVisibility(View.GONE);
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
}
