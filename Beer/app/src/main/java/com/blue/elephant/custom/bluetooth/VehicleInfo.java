package com.blue.elephant.custom.bluetooth;

import android.content.Context;

import com.blue.elephant.R;


/**
 * Created by song on 2018/7/6.
 */

public class VehicleInfo {

    private Context mContext;
    private String mDeadline;
    private String mElecLock;
    private String mGuardStatus;
    public String mVersion;
    public String mElectric;

    public boolean isElectOpen;
    public boolean isGuarded;
    public boolean isLowEnergy; //低电状态
    public boolean isHealthy = false;

    public long Qmax;
    public long Qremainder;

    public String electricityPercent;//剩余电量百分比
    public String strRemainderMileage;//剩余里程
    public String mErrorStatus="";
    //test
    private int communicationMode = 0;//通讯模式
    private double hallToKm;//霍尔转千米数(hallToKm=RATIO*motorMperimeter/(HALL*poleNumber*1000)/gearRatioMotor*gearRatioWheel)
    final private static double RATIO=0.678/0.5;//数据传输补偿


    public VehicleInfo(Context mContext)
    {
        this.mContext = mContext;
    }

    public String getDeadline(byte[] mByte)
    {
        int mDay= FormatUtil.unsignedByteToInt(mByte[2])<<8|FormatUtil.unsignedByteToInt(mByte[3]);
        int mHour=FormatUtil.unsignedByteToInt(mByte[4]);
        int mMin=FormatUtil.unsignedByteToInt(mByte[5]);
        int mSec=FormatUtil.unsignedByteToInt(mByte[6]);
        mDeadline = mContext.getResources().getString(R.string.oad_vehicle_deadline,mDay,mHour,mMin,mSec);
        return mDeadline;
    }

    public String getElecLock(byte[] mByte)
    {
        if(mByte[1]==0x01){
            isElectOpen=true;
            mElecLock = mContext.getResources().getString(R.string.oad_electric_open);
        }
        else{
            isElectOpen=false;
            mElecLock = mContext.getResources().getString(R.string.oad_electric_close);
        }
        return mElecLock;
    }

    public String getGuardStatus(byte[] mByte)
    {
        mGuardStatus = mContext.getResources().getString(R.string.oad_data_error);
        switch (mByte[1])
        {
            case 0X00:
                isGuarded = false;
                mGuardStatus = mContext.getResources().getString(R.string.oad_unlock_statue);
                break;
            case 0X01:
                isGuarded = true;
                mGuardStatus = mContext.getResources().getString(R.string.oad_controller_disconnected);
                break;
            case 0X02:
                isGuarded = true;
                mGuardStatus = mContext.getResources().getString(R.string.oad_lease_expires);
                break;
            case 0X03:
                isGuarded = true;
                mGuardStatus = mContext.getResources().getString(R.string.oad_at_comd);
                break;
            case 0X04:
                isGuarded = true;
                mGuardStatus = mContext.getResources().getString(R.string.oad_remote_control_lock);
                break;
            default:
                isGuarded = true;
                break;
        }
        return mGuardStatus;
    }

    public String getVersion(byte[] mByte)
    {
        int[] pcbVersion={0,0};//控制器版本
        int[] hardVersion={0,0,0};//固件版本
        int[] mData=FormatUtil.arrayByteToInt(mByte);
        pcbVersion[0]=mData[1];
        pcbVersion[1]=mData[2];
        hardVersion[0]=mData[3];
        hardVersion[1]=mData[4];
        hardVersion[2]=mData[5];
        mVersion = hardVersion[0] + "." + hardVersion[1] + "." + hardVersion[2];
        String PCB_version_number ="PCB_version_number :" +  pcbVersion[0]+"."+pcbVersion[1];
        String Firmware_version_number = "Firmware version number :"+ mVersion;
        return mVersion;
    }

    public String getElectric(byte[] mByte)
    {
        Qmax =FormatUtil.bytesTolong(mByte[4],mByte[3],mByte[2],mByte[1]);  //总电量
        Qremainder=FormatUtil.bytesTolong(mByte[8],mByte[7],mByte[6],mByte[5]);//剩余电量
        long Qthis=FormatUtil.bytesTolong(mByte[12],mByte[11],mByte[10],mByte[9]);  //本次行驶电量

        double Q_DEVIATION=0; //电量偏差
        if(Qmax==0){
            isLowEnergy=true;
            electricityPercent="0.5%";
        }
        else{
            isLowEnergy=100*Qremainder/Qmax<=(100*Q_DEVIATION);
            if(isLowEnergy){
                electricityPercent=100*Qremainder/Qmax+"%";
            }
            else{
                electricityPercent=Math.round((100 * Qremainder / Qmax - 100 * Q_DEVIATION) / (1 - Q_DEVIATION))+"%";
            }
        }
        mElectric = mContext.getString(R.string.oad_vehicle_electric,Qmax,Qremainder,Qthis,electricityPercent);
        return mElectric;
    }


    /**
     * 获取里程
     * @param mByte 从蓝牙获取的数据
     * @return 调试信息
     */
    public String getMileage(byte[] mByte){
        double maxMileage;//总里程
        double remainderMileage;//剩余里程
        double thisMileage;//本次行驶里程
        String strMaxMileage;//总里程
        String strThisMileage;//本次行驶里程
        String Kilometer= mContext.getResources().getString(R.string.oad_kilometer);
        if(communicationMode==0X00){
            hallToKm/=RATIO;
        }
        maxMileage=hallToKm* FormatUtil.bytesTolong(mByte[4],mByte[3],mByte[2],mByte[1]);
        remainderMileage=hallToKm* FormatUtil.bytesTolong(mByte[8],mByte[7],mByte[6],mByte[5]);
        thisMileage=hallToKm* FormatUtil.bytesTolong(mByte[12],mByte[11],mByte[10],mByte[9]);
        strMaxMileage=FormatUtil.setPrecision(maxMileage,2)+Kilometer;
        remainderMileage=getNewRemainderMileage(Qmax,Qremainder,remainderMileage);
        strRemainderMileage=FormatUtil.setPrecision(remainderMileage,0)+Kilometer;
        strThisMileage=FormatUtil.setPrecision(thisMileage,2)+Kilometer;
        return  mContext.getResources().getString(R.string.oad_total_mileage)+strMaxMileage+"\n"+mContext.getResources().getString(R.string.oad_remaining_mileage)+strRemainderMileage+"\n"+ mContext.getResources().getString(R.string.oad_this_mileage)+strThisMileage;
    }

    private double getNewRemainderMileage(long Qmax,long Qremainder,double vaule) {
        double remainderMileage = 0;//剩余里程
        float elect=(float)Qremainder/(float) Qmax;
        if(elect>=0.5){
            remainderMileage=vaule;
        }else if(elect>=0.25&&elect<=0.5){
            remainderMileage=1.26*elect*100-31.5;
        }else if(elect<0.25){
            remainderMileage=0;
        }
        return remainderMileage;
    }


    public String getErrorStatus(byte[] mByte) {
        int[] mData = FormatUtil.arrayByteToInt(mByte);
        mErrorStatus = "";
        if (mData[1] != 0)
        {
            isHealthy  = false;
        }
        else
        {
            isHealthy  = true;

        }
        if((mData[1]&0B1)>0 )
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_motor_faulty)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_motor_normal)+ "\n";
//        }
        if((mData[1]&0B1000)>0 )
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_protected_faulty)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_protected_normal)+ "\n";
//        }
        if((mData[1]&0B10000)>0 )
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_controller_faulty)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_controller_normal)+ "\n";
//        }
        if((mData[1]& 0B100000) > 0 )
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_handle_faulty)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_handle_normal)+ "\n";
//        }
        if((mData[1]& 0B1000000) > 0)
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_moter_hall_failure)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_moter_hall_normal)+ "\n";
//        }
        if((mData[1]& 0B10000000) > 0)
        {
            mErrorStatus += mContext.getResources().getString(R.string.oad_line_failure)+ "\n";
        }
//        else
//        {
//            mErrorStatus += mContext.getResources().getString(R.string.oad_line_normal)+ "\n";
//        }
        if(mErrorStatus.equals(""))
        {
            mErrorStatus = mContext.getResources().getString(R.string.oad_on_problem)+ "\n";
        }
        return mErrorStatus;
    }

    public String getDeviceInfo(byte[] mByte)
    {
        int[] mData = FormatUtil.arrayByteToInt(mByte);
        String msg = "";
        communicationMode = mData[2];
        switch (mData[2])
        {
            case 0X00:
                msg += mContext.getResources().getString(R.string.oad_comm_universal);
                break;
            case 0X01:
                msg += mContext.getResources().getString(R.string.oad_comm_touch);
                break;
            case 0X02:
                msg += mContext.getResources().getString(R.string.oad_omm_double);
                break;
        }
        double gearRatioMotor=mData[3]; //电机齿轮变速比
        double gearRatioWheel=mData[4];//后轮齿轮变速比
        double poleNumber=mData[5];  //极对数
        double wheelDiameter=mData[6]; //轮径
        double batteryAH=mData[7];//电池安时数
        int enduranceFunction=mData[8];//是否有续航功能,有:0x01;无:0x0
        double motorMperimeter=Math.PI*2.54*wheelDiameter/100;//电机周长(motorMperimeter=Math.PI*2.54*wheelDiameter/100)
        hallToKm=RATIO*motorMperimeter/(6 * poleNumber*1000)/gearRatioMotor*gearRatioWheel;
        return msg;
    }


}
