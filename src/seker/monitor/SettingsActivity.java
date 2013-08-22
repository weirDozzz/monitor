/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor;

import seker.common.BaseActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 设置Activity
 * 
 * @author liuxinjian
 * @since 2013-8-22
 */
public class SettingsActivity extends BaseActivity implements OnClickListener {
    /** 是否显示实时监控浮动框的key */
    public static final String KEY_FLOAT = "key_float_checkbox";
    /** 监控数据采样时间间隔的key */
    public static final String KEY_TIME = "key_time_edittext";
    /** 监控数据采样时间间隔最大值(单位为妙) */
    public static final int MAX_DURATION = 600;
    /** 监控数据采样时间间隔默认值(单位为妙) */
    public static final int DEFAULT_DURATION = 5;
    /** 是否显示实时监控浮动框 */
    private CheckBox mFloatCkBox;
    /** 实时监控数据采样时间间隔编辑框 */
    private EditText mTimeEdt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mFloatCkBox = (CheckBox) findViewById(R.id.floating);
        mFloatCkBox.setChecked(sp.getBoolean(KEY_FLOAT, true));
        mTimeEdt = (EditText) findViewById(R.id.time);
        mTimeEdt.setText(String.valueOf(sp.getInt(KEY_TIME, DEFAULT_DURATION)));
        mTimeEdt.setSelection(mTimeEdt.getText().toString().length());
        
        findViewById(R.id.save).setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        String str = mTimeEdt.getText().toString().trim();
        if (!TextUtils.isEmpty(str)) {
            if (TextUtils.isDigitsOnly(str)) {
                try {
                    int time = Integer.parseInt(str);
                    if (time <= MAX_DURATION && time > 0) {
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putBoolean(KEY_FLOAT, mFloatCkBox.isChecked());
                        editor.putInt(KEY_TIME, time);
                        Toast.makeText(SettingsActivity.this, R.string.valid_time, Toast.LENGTH_LONG).show();
                        editor.commit();
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.invalid_time3, Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(SettingsActivity.this, R.string.invalid_time2, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(SettingsActivity.this, R.string.invalid_time2, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(SettingsActivity.this, R.string.invalid_time1, Toast.LENGTH_LONG).show();
        }
    }
}
