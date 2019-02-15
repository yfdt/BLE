package com.blue.elephant.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UploadActivity extends BaseActivity {

    private File tempFile;
    private ImageView mVhiclePicture;
    private RelativeLayout mEmptyPicture,mCammera;

    private String mImagePath;
    private String mBikeCode;
    private String mBluetooth;
    private String mBikeID;

    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //剪裁请求码
    private static final int CROP_REQUEST_CODE = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


       ImageView actionBarLeft = findViewById(R.id.actionbar_left);
       TextView actionBarTitle = findViewById(R.id.actionbar_title);
        mVhiclePicture = findViewById(R.id.upload_picture);
        mEmptyPicture = findViewById(R.id.upload_empty);
        mCammera = findViewById(R.id.upload_pic_container);

        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.upload_title);


        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadActivity.this.finish();
            }
        });

        mCammera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhone();
            }
        });

        TextView mSubmit = findViewById(R.id.upload_submit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mImagePath== null)
                {
                    mVhiclePicture.setImageResource(R.mipmap.upload_error);
                    DialogUtils.showToast(UploadActivity.this,R.string.upload_pic_error);
                    return ;
                }
//                Log.i("Upload","" + mImagePath);
                Intent intent = new Intent(UploadActivity.this,InsuranceActivity.class);
                intent.putExtra("ImagePath",mImagePath);
                intent.putExtra("BikeCode",mBikeCode);
                intent.putExtra("Bluetooth",mBluetooth);
                intent.putExtra("BikeID",mBikeID);
                startActivity(intent);
                UploadActivity.this.finish();
            }
        });

        mBikeCode = getIntent().getStringExtra("BikeCode");
        mBluetooth = getIntent().getStringExtra("Bluetooth");
        mBikeID = getIntent().getStringExtra("BikeID");
    }


    private void takePhone()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        //处理6.0以上权限获取问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success

                }
                break;
        }
    }





    private void getPicture()
    {
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(UploadActivity.this, "com.hansion.chosehead", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
//            intent.setDataAndType( contentUri, "image/*");
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
//            intent.setDataAndType( Uri.fromFile(tempFile), "image/*");
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap mBitmap = (Bitmap) extras.get("data");
            mVhiclePicture.setImageBitmap(mBitmap);
            mVhiclePicture.setVisibility(View.VISIBLE);
            mEmptyPicture.setVisibility(View.GONE);
            mImagePath = saveImage("userHeader",mBitmap);
        }


    }

    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);

    }


    /**
     * 保存图片到本地
     *
     * @param name
     * @param bmp
     * @return
     */
    public String saveImage(String name, Bitmap bmp) {
//        File appDir = new File(Environment.getExternalStorageDirectory().getPath());
//        if (!appDir.exists()) {
//            appDir.mkdir();
//        }
        String fileName =Environment.getExternalStorageDirectory().getPath() + "/"+  name + ".jpg";
//        Log.i("Upload","path : " + fileName );
        File file = new File(fileName);
        try {
            if (!file.exists()) {	//文件不存在则创建文件，先创建目录
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
