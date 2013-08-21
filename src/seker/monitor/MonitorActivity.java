/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import seker.common.BaseActivity;
import seker.monitor.process.ProcessInfo;
import seker.monitor.process.ProcessUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主Activity：入口
 * 
 * @author liuxinjian
 * @since 2013-8-20
 */
public class MonitorActivity extends BaseActivity {

    /** Log Switch */
    private static final String TAG = "MonitorActivity";

    /** 启动一个APP，等待其启动完成的Time out时间 */
    private static final int TIMEOUT = 20000;

    /** 用于启动/停止MonitorService的Intent */
    private Intent mMonitorServiceIntent;

    private ListView lstViProgramme;
    private Button btnTest;
    private boolean isTesting = true;
    private boolean isRadioChecked = false;
    private int pid, uid;
    private String processName, packageName, settingTempFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.mainpage);
        createNewFile();
        lstViProgramme = (ListView) findViewById(R.id.processList);
        btnTest = (Button) findViewById(R.id.test);
        lstViProgramme.setAdapter(new ProcessListAdapter());
        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonitorServiceIntent = new Intent();
                mMonitorServiceIntent.setClass(MonitorActivity.this, MonitorService.class);
                if (isTesting) {
                    if (isRadioChecked == true) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                        Log.d(TAG, packageName);
                        startActivity(intent);
                        waitForAppStart(packageName);
                        mMonitorServiceIntent.putExtra("processName", processName);
                        mMonitorServiceIntent.putExtra("pid", pid);
                        mMonitorServiceIntent.putExtra("uid", uid);
                        mMonitorServiceIntent.putExtra("packageName", packageName);
                        mMonitorServiceIntent.putExtra("settingTempFile", settingTempFile);
                        startService(mMonitorServiceIntent);
                        btnTest.setText("停止测试");
                        isTesting = false;
                    } else {
                        Toast.makeText(MonitorActivity.this, "请选择需要测试的应用程序", Toast.LENGTH_LONG).show();
                    }
                } else {
                    btnTest.setText("开始测试");
                    isTesting = true;
                    Toast.makeText(MonitorActivity.this, "测试结果文件：" + MonitorService.resultFilePath, Toast.LENGTH_LONG)
                            .show();
                    stopService(mMonitorServiceIntent);
                }
            }
        });
    }

    /**
     * create new file to reserve setting data
     */
    private void createNewFile() {
        Log.i(TAG, "create new file to save setting data");
        settingTempFile = getBaseContext().getFilesDir().getPath() + "\\Emmagee_Settings.txt";
        File settingFile = new File(settingTempFile);
        if (!settingFile.exists())
            try {
                settingFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(settingFile)));
                bw.write("5" + "\r\n" + "true");
                bw.close();
            } catch (IOException e) {
                Log.d(TAG, "create new file exception :" + e.getMessage());
            }
    }

    /**
     * wait for test application started , timeout is 20s
     * 
     * @param packageName
     *            package name of test application
     */
    private void waitForAppStart(String packageName) {
        Log.d(TAG, "wait for app start");
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            List<ProcessInfo> processes = ProcessUtils.getRunningProcess(getBaseContext());
            for (ProcessInfo processInfo : processes) {
                if ((processInfo.getPackageName() != null) && (processInfo.getPackageName().equals(packageName))) {
                    pid = processInfo.getPid();
                    Log.d(TAG, "pid:" + pid);
                    uid = processInfo.getUid();
                    if (pid != 0) {
                        isProcessStarted = true;
                        break;
                    }
                }
            }
            if (isProcessStarted)
                break;
        }
    }

    /**
     * override return key to show a dialog
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showDialog(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * set menu options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, 0, "退出").setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, Menu.FIRST, 1, "设置").setIcon(android.R.drawable.ic_menu_directions);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
        case 0:
            showDialog(0);
            break;
        case 1:
            Intent intent = new Intent();
            intent.setClass(MonitorActivity.this, SettingsActivity.class);
            intent.putExtra("settingTempFile", settingTempFile);
            startActivityForResult(intent, Activity.RESULT_FIRST_USER);
            break;
        default:
            break;
        }
        return false;
    }

    /**
     * create a dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0:
            return new AlertDialog.Builder(this).setTitle("确定退出程序？")
                    .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mMonitorServiceIntent != null) {
                                Log.d(TAG, "stop service");
                                stopService(mMonitorServiceIntent);
                            }
                            Log.d(TAG, "exit Emmagee");
                            MonitorService.closeOpenedStream();
                            finish();
                            System.exit(0);
                        }
                    }).setNegativeButton("取消", null).create();
        default:
            return null;
        }
    }

    /**
     * Customizing process adapter
     */
    private class ProcessListAdapter extends BaseAdapter {
        List<ProcessInfo> mProcessInfoList;
        int tempPosition = -1;

        class ViewHolder {
            RadioButton rdBtn;
            ImageView appIcon;
            TextView appName;
            TextView isRunning;
        }

        public ProcessListAdapter() {
            mProcessInfoList = ProcessUtils.getRunningProcess(getBaseContext());
        }

        @Override
        public int getCount() {
            return mProcessInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mProcessInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = buildView();
            }
            
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final int i = position;
            convertView = MonitorActivity.this.getLayoutInflater().inflate(R.layout.process_list_item, null);
            holder.rdBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isRadioChecked = true;
                        // Radio function
                        if (tempPosition != -1) {
                            RadioButton tempButton = (RadioButton) findViewById(tempPosition);
                            if ((tempButton != null) && (tempPosition != i)) {
                                tempButton.setChecked(false);
                            }
                        }

                        tempPosition = buttonView.getId();
                        packageName = mProcessInfoList.get(tempPosition).getPackageName();
                        processName = mProcessInfoList.get(tempPosition).getProcessName();
                    }
                }
            });
            if (tempPosition == position) {
                if (!holder.rdBtn.isChecked())
                    holder.rdBtn.setChecked(true);
            }
            ProcessInfo pr = (ProcessInfo) mProcessInfoList.get(position);
            holder.appIcon.setImageDrawable(pr.getIcon());
            holder.appName.setText(pr.getProcessName());
            return convertView;
        }
        
        private View buildView() {
            View view = getLayoutInflater().inflate(R.layout.process_list_item, null);
            
            ViewHolder holder = new ViewHolder();
            view.setTag(holder);
            
            holder.rdBtn = (RadioButton) view.findViewById(R.id.rb);
            holder.appName = (TextView) view.findViewById(R.id.name);
            holder.isRunning = (TextView) view.findViewById(R.id.running);
            holder.appIcon = (ImageView) view.findViewById(R.id.image);
            
            return view;
        }
    }
}
