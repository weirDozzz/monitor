
package com.baidu.searchbox.util.monitor.status;

import android.graphics.drawable.Drawable;

public class Programe {
	private Drawable mIcon;
	private String mProcessName;
	private String mPackageName;
	private int mPid;
	private int mUid;

	public int getUid() {
		return mUid;
	}

	public void setUid(int uid) {
		this.mUid = uid;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		this.mIcon = icon;
	}

	public String getProcessName() {
		return mProcessName;
	}

	public void setProcessName(String processName) {

		this.mProcessName = processName;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	public int getPid() {
		return mPid;
	}

	public void setPid(int pid) {
		this.mPid = pid;
	}
}
