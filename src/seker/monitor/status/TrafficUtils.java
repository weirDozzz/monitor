/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package seker.monitor.status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 监测网络传输信息的工具类
 * 
 * @author liuxinjian
 * @since 2013-8-22
 */
public final class TrafficUtils {

    /** 传输信息文件的路径 */
    public static final String TRAFFIC_INFO_PATH = "/proc/uid_stat/";

    /** 上传信息文件的文件名 */
    public static final String TCP_SND = "/tcp_snd";

    /** 下载信息文件的文件名 */
    public static final String TCP_RCV = "/tcp_rcv";

    /**
     * 工具类，构造方法私有
     */
    private TrafficUtils() {

    }

    /**
     * get total network traffic, which is the sum of upload and download
     * traffic
     * 
     * @return total traffic include received and send traffic
     */
    public static long getTrafficInfo(String uid) {
        final String rcvPath = TRAFFIC_INFO_PATH + uid + TCP_RCV;
        final String sndPath = TRAFFIC_INFO_PATH + uid + TCP_SND;
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
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rcvTraffic == -1 || sndTraffic == -1) {
            return -1;
        } else {
            return (rcvTraffic + sndTraffic);
        }
    }
}
