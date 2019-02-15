package com.blue.elephant.custom.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2018/5/2.
 */

public interface BlueCallback {

    /***
     * 连接设备的状态
     * @param statue
     */
    public void onConnect(int statue);

    /***
     * 设备接收的消息
     * @param message
     */
    public void onReceive(byte[] message);


    /***
     * 读当前的设备信息
     * @param  message 当前蓝牙设备的数据
     * @param state 标识  1 当前的软件版本号
     */
    public void onRead(String message, int state);

    /***
     * 扫描当前的设备
     * @param mDevice
     */
    public void onDevice(BluetoothDevice mDevice,int sign);


    /***
     * 显示OAD升级的进度
     * @param percent 百分比
     */
    public void onProgress(String percent);


}
