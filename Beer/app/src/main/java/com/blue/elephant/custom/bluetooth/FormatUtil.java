package com.blue.elephant.custom.bluetooth;

import android.content.Context;
import android.util.TypedValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by song on 2018/6/26.
 */

public class FormatUtil {

    /**
     * 手机验证
     * @param str 手机号
     * @return true
     *         false
     */
    public static boolean isMobile(String str) {
        // 手机验证规则
        String regEx = "^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$";
        // 编译正则表达式
        return Pattern.matches(regEx, str);
    }


    /**
     * 将JSONArray解析成ArrayList<String>
     */
    public static ArrayList<String> jsonArrayToArrayListString(JSONArray jsonArray){
        ArrayList<String> list = new ArrayList<>();
        try{
            for (int i=0; i<jsonArray.length(); i++) {
                list.add( jsonArray.getString(i) );
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 将JSONArray解析成ArrayList<JSONObject>
     */
    public static ArrayList<JSONObject> jsonArrayToArrayListJSONObject(JSONArray jsonArray){
        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject=jsonArray.optJSONObject(i);
            list.add(jsonObject);
        }
        return list;
    }

    /**
     * 将JSONArray解析成ArrayList<String>
     */
    public static ArrayList<String> getJSONObjectKeys(JSONObject jsonObject) {
        ArrayList<String> list = new ArrayList<>();
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            list.add(key);
        }
        return list;
    }

    /**
     * 浮点数精度转换
     * @param mData 浮点数
     * @param Digit 精确位数
     * @return
     */
    public static String setPrecision(double mData,int Digit){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(Digit);
        return nf.format(mData);
    }

    /**
     * 将数组转换成字符串输出
     * @param intArray
     * @return
     */
    public static String intArrToString(int[] intArray){
        String msg="";
        for (int i= 0; i< intArray.length; i++)
        {
            if(i==(intArray.length-1)){
                msg=msg+Integer.toHexString(intArray[i]);
            }
            else{
                msg=msg+Integer.toHexString(intArray[i])+",";
            }

        }
        return msg;
    }

    /**
     *  将时间转换成租期命令格式
     *  @param mDay 天数
     *  @param mHour 小时
     *  @param mMin 分钟
     *  @param mSecond 秒
     *  @return 租期命令
     */
    public static byte[] timeToRent(long mDay,long mHour,long mMin,long mSecond){

        byte[] rentTime={0x41,0x54,0x2B,0x44,0x65,0x61,0x64,0x4C,0x69,0x6E,0x65,0x3D,0x0,0x0,0x0,0x0,0x0};//H<=0x18 M<=3C S<=60
        rentTime[12]=(byte)(mDay>>8);
        rentTime[13]=(byte)mDay;
        rentTime[14]=(byte)mHour;
        rentTime[15]=(byte)mMin;
        rentTime[16]=(byte)mSecond;
        return rentTime;
    }

    /**
     * 将byte类型数组转为int类型数组
     * @params mByte数组
     * @return
     */
    public static int[] arrayByteToInt(byte[] mByte){
        int[] mInt=new int[mByte.length];
        for (int i= 0; i< mByte.length; i++)
        {
            mInt[i]=unsignedByteToInt(mByte[i]);
        }
        return mInt;
    }

    /**
     * 将4个byte类型的值合并为一个long类型的值
     * @param b0,b1,b2,b3
     */
    public static long bytesTolong(byte b0, byte b1, byte b2, byte b3) {
        long mLong= (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
        if(mLong<0)
        {
            mLong+=4294967296L;
        }
        return mLong;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    private float bytesToFloat(byte b0, byte b1) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
        return (float)(mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + (unsignedByteToInt(b1) << 8)
                + (unsignedByteToInt(b2) << 16), 24);
        return (float)(mantissa * Math.pow(10, b3));
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size-1)) != 0) {
            unsigned = -1 * ((1 << size-1) - (unsigned & ((1 << size-1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert an integer into the signed bits of a given length.
     */
    private int intToSignedBits(int i, int size) {
        if (i < 0) {
            i = (1 << size-1) + (i & ((1 << size-1) - 1));
        }
        return i;
    }


    public static  int dpToPix(float dp,Context mContext) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }
}
