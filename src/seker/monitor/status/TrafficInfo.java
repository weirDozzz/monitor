/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor.status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class TrafficInfo {

	private static final String LOG_TAG = "TrafficInfo";
	
	private String uid;
	
	public TrafficInfo(String uid){
		this.uid = uid;
	}

	/**
	 * get total network traffic, which is the sum of upload and download traffic
	 * 
	 * @return total traffic include received and send traffic
	 */
	public long getTrafficInfo() {
		Log.i(LOG_TAG,"get traffic information");
		String rcvPath = "/proc/uid_stat/" + uid + "/tcp_rcv";
		String sndPath = "/proc/uid_stat/" + uid + "/tcp_snd";
		long rcvTraffic = -1;
		long sndTraffic = -1;
		try {
			RandomAccessFile rafRcv = new RandomAccessFile(rcvPath, "r");
			rcvTraffic = Long.parseLong(rafRcv.readLine());
			rafRcv.close();
			
			RandomAccessFile rafSnd = new RandomAccessFile(sndPath, "r");
			sndTraffic = Long.parseLong(rafSnd.readLine());
			rafSnd.close();
		} catch (FileNotFoundException e) {
			rcvTraffic = -1;
			sndTraffic = -1;
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "NumberFormatException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		}
		if (rcvTraffic == -1 || sndTraffic == -1) {
			return -1;
		} else
			return (rcvTraffic + sndTraffic);
	}
}
