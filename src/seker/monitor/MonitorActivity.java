/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor;

import java.util.List;

import seker.common.BaseActivity;
import seker.monitor.process.ProcessInfo;
import seker.monitor.process.ProcessUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
public class MonitorActivity extends BaseActivity implements OnCheckedChangeListener, OnClickListener {

    /** Log Switch */
    public static final String TAG = "MonitorActivity";

    /** 启动一个APP，等待其启动完成的Time out时间 */
    private static final int TIMEOUT = 20000;

    /** 用于启动/停止MonitorService的Intent */
    private Intent mMonitorServiceIntent;

    /** 正在测试的APP Info */
    private ProcessInfo mCurProcessInfo;

    /** 可以进行测试 */
    private boolean isTesting = true;
    
    /** Dialog ID */
    private static final int DIALOG_ID = 1024;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mainpage);
        findViewById(R.id.test).setOnClickListener(this);

        ListView listview = (ListView) findViewById(R.id.list);
        listview.setCacheColorHint(Color.TRANSPARENT);
        List<ProcessInfo> processes = ProcessUtils.getRunningProcess(getApplicationContext());
        ProcessListAdapter adapter = new ProcessListAdapter(getApplicationContext(), processes, this);
        listview.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        Button btnTest = (Button) v;
        if (isTesting) {
            if (null != mCurProcessInfo) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(mCurProcessInfo.getPackageName());
                startActivity(intent);
                waitForAppStart(mCurProcessInfo.getPackageName());
                
                mMonitorServiceIntent = new Intent();
                mMonitorServiceIntent.setClass(MonitorActivity.this, MonitorService.class);
                mMonitorServiceIntent.putExtra(MonitorService.KEY_PROCESS_INFO, mCurProcessInfo);
                startService(mMonitorServiceIntent);
                
                btnTest.setText(R.string.test_end);
                isTesting = false;
            } else {
                btnTest.setText(R.string.test_start);
                Toast.makeText(MonitorActivity.this, R.string.select_app, Toast.LENGTH_LONG).show();
            }
        } else {
            btnTest.setText(R.string.test_start);
            isTesting = true;
            
            String str = getString(R.string.monitor_result) + MonitorService.resultFilePath;
            Toast.makeText(MonitorActivity.this, str, Toast.LENGTH_LONG).show();
            stopService(mMonitorServiceIntent);
            mMonitorServiceIntent = null;
        }
    }

    /**
     * wait for test application started , timeout is 20s
     * 
     * @param packageName
     *            package name of test application
     */
    private void waitForAppStart(String packageName) {
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            List<ProcessInfo> processes = ProcessUtils.getRunningProcess(getBaseContext());
            for (ProcessInfo processInfo : processes) {
                if ((processInfo.getPackageName() != null) && (processInfo.getPackageName().equals(packageName))) {
                    if (processInfo.getPid() != 0) {
                        break;
                    }
                }
            }
            if (isProcessStarted) {
                break;
            }
        }
    }

    /**
     * override return key to show a dialog
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showDialog(DIALOG_ID);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, 0, R.string.option).setIcon(android.R.drawable.ic_menu_directions);
        menu.add(0, Menu.FIRST, 1, R.string.exit).setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
        case 0:
            Intent intent = new Intent();
            intent.setClass(MonitorActivity.this, SettingsActivity.class);
            startActivity(intent);
            break;
        case 1:
            showDialog(DIALOG_ID);
            break;
        default:
            break;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
        case DIALOG_ID:
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_content)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mMonitorServiceIntent != null) {
                            stopService(mMonitorServiceIntent);
                        }
                        MonitorService.closeOpenedStream();
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
            dialog = builder.create();
        default:
            dialog = super.onCreateDialog(id);
            break;
        }
        return dialog;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ProcessInfo info = (ProcessInfo) buttonView.getTag();
        info.setIsSelected(isChecked);
        mCurProcessInfo = isChecked ? info : null;
    }
}

/**
 * Customizing process adapter
 * 
 * @author liuxinjian
 * @since 2013-8-21
 */
class ProcessListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ProcessInfo> mProcessInfoList;
    private OnCheckedChangeListener mListener;

    public ProcessListAdapter(Context context, List<ProcessInfo> infoList, OnCheckedChangeListener listener) {
        mContext = context.getApplicationContext();
        mProcessInfoList = infoList;
        mListener = listener;
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
        ProcessInfo info = (ProcessInfo) mProcessInfoList.get(position);

        if (null == convertView) {
            convertView = buildView(mContext, mListener);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.rdBtn.setChecked(info.isSelected());
        holder.rdBtn.setTag(info);
        holder.appIcon.setImageDrawable(info.getIcon());
        holder.appName.setText(info.getProcessName());
        holder.isRunning.setText(info.isRuning() ? R.string.running : R.string.not_running);

        return convertView;
    }

    private View buildView(Context context, OnCheckedChangeListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.process_list_item, null);

        ViewHolder holder = new ViewHolder();
        view.setTag(holder);

        holder.rdBtn = (RadioButton) view.findViewById(R.id.rb);
        holder.rdBtn.setOnCheckedChangeListener(listener);

        holder.appName = (TextView) view.findViewById(R.id.name);
        holder.isRunning = (TextView) view.findViewById(R.id.running);
        holder.appIcon = (ImageView) view.findViewById(R.id.image);

        return view;
    }

    /**
     * ViewHolder
     * 
     * @author liuxinjian
     * @since 2013-8-21
     */
    class ViewHolder {
        RadioButton rdBtn;
        ImageView appIcon;
        TextView appName;
        TextView isRunning;
    }
}
