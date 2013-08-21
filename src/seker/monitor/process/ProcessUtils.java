/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor.process;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * 此类用于查找可测试的APP
 * 
 * @author liuxinjian
 * @since 2013-8-21
 */
public final class ProcessUtils {
    /**
     * 工具类，隐藏其构造方法
     */
    private ProcessUtils() {
        
    }

    /**
     * get information of all running processes,including:
     *  package name, process name, icon, pid, uid
     * 
     * @param context Context
     * @return running processes list
     */
    public static synchronized List<ProcessInfo> getRunningProcess(Context context) {
        String packageName = context.getPackageName();
        
        List<ProcessInfo> progressList = new ArrayList<ProcessInfo>();

        PackageManager pm = context.getPackageManager();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<RunningAppProcessInfo> runingProcessList = am.getRunningAppProcesses();
        for (ApplicationInfo appInfo : appList) {
            // 过滤掉校系统APP，和Monitor程序自身
            if (((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                    || TextUtils.equals(packageName, appInfo.processName)) {
                continue;
            }
            
            ProcessInfo processInfo = new ProcessInfo();
            for (RunningAppProcessInfo runningAppProcessInfo : runingProcessList) {
                if (TextUtils.equals(runningAppProcessInfo.processName, appInfo.processName)) {
                    processInfo.setIsRuning(true);
                    processInfo.setPid(runningAppProcessInfo.pid);
                    processInfo.setUid(runningAppProcessInfo.uid);
                    break;
                }
            }
            processInfo.setPackageName(appInfo.processName);
            processInfo.setProcessName(appInfo.loadLabel(pm).toString());
            processInfo.setIcon(appInfo.loadIcon(pm));
            
            progressList.add(processInfo);
        }
        return progressList;
    }
}
