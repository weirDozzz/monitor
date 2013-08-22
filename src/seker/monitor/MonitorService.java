/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import seker.monitor.R;
import seker.monitor.process.ProcessInfo;
import seker.monitor.status.CpuInfo;
import seker.monitor.status.MemoryInfo;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 测试APP监视Service
 * 
 * @author liuxinjian
 * @since 2013-8-22
 */
public class MonitorService extends Service {

    /** WindowManager */
    private WindowManager mWindowManager;
    /** WindowManager.LayoutParams */
    private WindowManager.LayoutParams mWmParams = new WindowManager.LayoutParams();
    /** FloatingWindow */
    private View mFloatView;
    private TextView txtTotalMem;
    private TextView txtUnusedMem;
    private TextView txtTraffic;
    private ImageView imgViIcon;
    
    private float mTouchStartX;
    private float mTouchStartY;
    private float startX;
    private float startY;
    private float x;
    private float y;
    private int delaytime;
    private DecimalFormat fomart;
    private MemoryInfo memoryInfo;
    private WifiManager wifiManager;
    private Handler handler = new Handler();
    private CpuInfo cpuInfo;
    private String time;
    private boolean isFloating;
    /** 正在测试的APP Info */
    private ProcessInfo mCurProcessInfo;

    public static BufferedWriter bw;
    public static FileOutputStream out;
    public static OutputStreamWriter osw;
    public static String resultFilePath;

    /** 传递ProcessInfo的key */
    public static final String KEY_PROCESS_INFO = "key_process_info";

    @Override
    public void onCreate() {
        super.onCreate();
        memoryInfo = new MemoryInfo();
        fomart = new DecimalFormat();
        fomart.setMaximumFractionDigits(2);
        fomart.setMinimumFractionDigits(0);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        setForeground(true);
        super.onStart(intent, startId);

        mCurProcessInfo = (ProcessInfo) intent.getExtras().getSerializable(KEY_PROCESS_INFO);

        cpuInfo = new CpuInfo(getBaseContext(), mCurProcessInfo.getPid(), Integer.toString(mCurProcessInfo.getUid()));
        delaytime = Integer.parseInt(time) * 1000;
        if (isFloating) {
            mFloatView = LayoutInflater.from(this).inflate(R.layout.floating, null);
            txtUnusedMem = (TextView) mFloatView.findViewById(R.id.memunused);
            txtTotalMem = (TextView) mFloatView.findViewById(R.id.memtotal);
            txtTraffic = (TextView) mFloatView.findViewById(R.id.traffic);
            Button btnWifi = (Button) mFloatView.findViewById(R.id.wifi);

            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                btnWifi.setText(R.string.closewifi);
            } else {
                btnWifi.setText(R.string.openwifi);
            }
            txtUnusedMem.setText("计算中,请稍后...");
            txtUnusedMem.setTextColor(android.graphics.Color.RED);
            txtTotalMem.setTextColor(android.graphics.Color.RED);
            txtTraffic.setTextColor(android.graphics.Color.RED);
            imgViIcon = (ImageView) mFloatView.findViewById(R.id.img2);
            imgViIcon.setVisibility(View.GONE);
            createFloatingWindow(btnWifi);
        }
        createResultCsv();
        handler.postDelayed(task, delaytime);
    }

    /**
     * write the test result to csv format report
     */
    private void createResultCsv() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String mDateTime = formatter.format(cal.getTime().getTime());

        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            resultFilePath = android.os.Environment.getExternalStorageDirectory() + File.separator
                    + "Emmagee_TestResult_" + mDateTime + ".csv";
        } else {
            resultFilePath = getBaseContext().getFilesDir().getPath() + File.separator + "Emmagee_TestResult_"
                    + mDateTime + ".csv";
        }
        try {
            File resultFile = new File(resultFilePath);
            resultFile.createNewFile();
            out = new FileOutputStream(resultFile);
            osw = new OutputStreamWriter(out, "utf-8");
            bw = new BufferedWriter(osw);
            long totalMemorySize = memoryInfo.getTotalMemory();
            String totalMemory = fomart.format((double) totalMemorySize / 1024);
            bw.write("指定应用的CPU内存监控情况\r\n" + "应用包名：," + mCurProcessInfo.getPackageName() + "\r\n" + "应用名称: ,"
                    + mCurProcessInfo.getProcessName() + "\r\n" + "应用PID: ," + mCurProcessInfo.getPid() + "\r\n"
                    + "机器内存大小(MB)：," + totalMemory + "MB\r\n" + "机器CPU型号：," + cpuInfo.getCpuName() + "\r\n"
                    + "机器android系统版本：," + memoryInfo.getSDKVersion() + "\r\n" + "手机型号：," + memoryInfo.getPhoneType()
                    + "\r\n" + "UID：," + mCurProcessInfo.getUid() + "\r\n");
            bw.write("时间" + "," + "应用占用内存PSS(MB)" + "," + "应用占用内存比(%)" + "," + " 机器剩余内存(MB)" + "," + "应用占用CPU率(%)"
                    + "," + "CPU总使用率(%)" + "," + "流量(KB)：" + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create floating window
     */
    private void createFloatingWindow(Button btnWifi) {
        SharedPreferences shared = getSharedPreferences("float_flag", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("float", 1);
        editor.commit();
        mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mWmParams.type = 2002;
        mWmParams.flags |= 8;
        mWmParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWmParams.x = 0;
        mWmParams.y = 0;
        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.format = 1;
        mWindowManager.addView(mFloatView, mWmParams);
        mFloatView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY() - 25;
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // state = MotionEvent.ACTION_DOWN;
                    startX = x;
                    startY = y;
                    mTouchStartX = event.getX();
                    mTouchStartY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // state = MotionEvent.ACTION_MOVE;
                    updateViewPosition();
                    break;

                case MotionEvent.ACTION_UP:
                    // state = MotionEvent.ACTION_UP;
                    updateViewPosition();
                    showImg();
                    mTouchStartX = mTouchStartY = 0;
                    break;
                }
                return true;
            }
        });

        btnWifi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Button btnWifi = (Button) mFloatView.findViewById(R.id.wifi);
                    String buttonText = (String) btnWifi.getText();
                    String wifiText = getResources().getString(R.string.openwifi);
                    if (buttonText.equals(wifiText)) {
                        wifiManager.setWifiEnabled(true);
                        btnWifi.setText(R.string.closewifi);
                    } else {
                        wifiManager.setWifiEnabled(false);
                        btnWifi.setText(R.string.openwifi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mFloatView.getContext(), "操作wifi失败", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * show the image
     */
    private void showImg() {
        if (Math.abs(x - startX) < 1.5 && Math.abs(y - startY) < 1.5 && !imgViIcon.isShown()) {
            imgViIcon.setVisibility(View.VISIBLE);
        } else if (imgViIcon.isShown()) {
            imgViIcon.setVisibility(View.GONE);
        }
    }

    private Runnable task = new Runnable() {
        public void run() {
            dataRefresh();
            handler.postDelayed(this, delaytime);
            if (isFloating)
                mWindowManager.updateViewLayout(mFloatView, mWmParams);
        }
    };

    /**
     * refresh the data showing in floating window
     * 
     * @throws FileNotFoundException
     * 
     * @throws IOException
     */
    private void dataRefresh() {
        int pidMemory = memoryInfo.getPidMemorySize(mCurProcessInfo.getPid(), getBaseContext());
        long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
        String freeMemoryKb = fomart.format((double) freeMemory / 1024);
        String processMemory = fomart.format((double) pidMemory / 1024);
        ArrayList<String> processInfo = cpuInfo.getCpuRatioInfo();
        if (isFloating) {
            String processCpuRatio = "0";
            String totalCpuRatio = "0";
            String trafficSize = "0";
            int tempTraffic = 0;
            double trafficMb = 0;
            boolean isMb = false;
            if (!processInfo.isEmpty()) {
                processCpuRatio = processInfo.get(0);
                totalCpuRatio = processInfo.get(1);
                trafficSize = processInfo.get(2);
                if (trafficSize != null && !trafficSize.equals("") && !trafficSize.equals("-1")) {
                    tempTraffic = Integer.parseInt(trafficSize);
                    if (tempTraffic > 1024) {
                        isMb = true;
                        trafficMb = (double) tempTraffic / 1024;
                    }
                }
            }
            if (processCpuRatio != null && totalCpuRatio != null) {
                txtUnusedMem.setText("占用内存:" + processMemory + "MB" + ",机器剩余:" + freeMemoryKb + "MB");
                txtTotalMem.setText("占用CPU:" + processCpuRatio + "%" + ",总体CPU:" + totalCpuRatio + "%");
                if (trafficSize.equals("-1")) {
                    txtTraffic.setText("本程序或本设备不支持流量统计");
                } else if (isMb)
                    txtTraffic.setText("消耗流量:" + fomart.format(trafficMb) + "MB");
                else
                    txtTraffic.setText("消耗流量:" + trafficSize + "KB");
            }
        }
    }

    /**
     * update the position of floating window
     */
    private void updateViewPosition() {
        mWmParams.x = (int) (x - mTouchStartX);
        mWmParams.y = (int) (y - mTouchStartY);
        mWindowManager.updateViewLayout(mFloatView, mWmParams);
    }

    /**
     * close all opened stream
     */
    public static void closeOpenedStream() {
        try {
            if (bw != null)
                bw.close();
            if (osw != null)
                osw.close();
            if (out != null)
                out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null)
            mWindowManager.removeView(mFloatView);
        handler.removeCallbacks(task);
        closeOpenedStream();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
