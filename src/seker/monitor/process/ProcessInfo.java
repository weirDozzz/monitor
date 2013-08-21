/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor.process;

import android.graphics.drawable.Drawable;

/**
 * 主界面上显示可测试的APP列表，此类代表单项APP所显示的信息。
 * 
 * @author liuxinjian
 * @since 2013-8-21
 */
public class ProcessInfo {
    /** 可测试的APP Icon */
    private Drawable mIcon;
    /** 本Monitor程序启动的时刻，该测试的APP是否正在运行中 */
    private boolean mIsRuning;
    /** 可测试的APP 包名 */
    private String mPackageName;
    /** 可测试的APP 进程ID */
    private int mPid;
    /** 可测试的APP 应用程序Name */
    private String mProcessName;
    /** 可测试的APP 用户ID */
    private int mUid;

    /**
     * @return the mIcon
     */
    public Drawable getIcon() {
        return mIcon;
    }

    /**
     * @return the mPackageName
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * @return the mPid
     */
    public int getPid() {
        return mPid;
    }

    /**
     * @return the mProcessName
     */
    public String getProcessName() {
        return mProcessName;
    }

    /**
     * @return the mUid
     */
    public int getUid() {
        return mUid;
    }

    /**
     * @return the mIsRuning
     */
    public boolean isIsRuning() {
        return mIsRuning;
    }

    /**
     * @param icon
     *            the mIcon to set
     */
    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    /**
     * @param mIsRuning the mIsRuning to set
     */
    public void setIsRuning(boolean mIsRuning) {
        this.mIsRuning = mIsRuning;
    }

    /**
     * @param packageName
     *            the mPackageName to set
     */
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    /**
     * @param pid
     *            the mPid to set
     */
    public void setPid(int pid) {
        mPid = pid;
    }

    /**
     * @param processName
     *            the mProcessName to set
     */
    public void setProcessName(String processName) {
        mProcessName = processName;
    }

    /**
     * @param uid
     *            the mUid to set
     */
    public void setUid(int uid) {
        mUid = uid;
    }
    
    @Override
    public String toString() {
        return "ProcessInfo [mIcon=" + mIcon + ", mIsRuning=" + mIsRuning + ", mPackageName=" + mPackageName
                + ", mPid=" + mPid + ", mProcessName=" + mProcessName + ", mUid=" + mUid + "]";
    }
}
