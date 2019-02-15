package com.blue.elephant.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


import com.blue.elephant.R;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xutils.common.Callback;
import org.xutils.http.HttpManagerImpl;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by song on 2018/6/25.
 */

public class NetUtil {

    private static Context mContext;

    /**
     * 判断网络是否存在
     *
     * @param ctx
     * @return true:有
     * false:无
     */
    public static boolean hasNet(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(ctx.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * 网络发送请求响应回调
     */
    public interface CallBack {
        /**
         * 请求得到的响应字符串
         *
         * @param response 服务器响应值
         */
        void onResponse(String response);
    }

    /**
     * Xutils框架发送网络请求
     */
    public static void doXutilsHttp(final Context ctx, RequestParams params, final CallBack callBack) {

        params.addHeader("appversion", String.valueOf(AppUtil.getVersionCode(ctx)));
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callBack.onResponse(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                if (ex instanceof SocketTimeoutException) {
                    Log.e("NetUtil","连接超时1");
                    DialogUtils.showToast(ctx,R.string.connect_time_out);
                }

                if (ex instanceof ConnectException) {
                    Log.e("NetUtil","连接超时2");
                    //song 加载结束取消加载框
//                    DialogUtil.dismissProgressDialog();
                    DialogUtils.showToast(ctx,R.string.connect_time_out);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                Log.e("NetUtil","onCancelled has  exception ： "+ cex.getMessage());
            }

            @Override
            public void onFinished() {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                Log.e("NetUtil","onFinished" );
            }
        });
    }

    public static void doHttp(final Context ctx, RequestParams params, final com.blue.elephant.util.CallBack callBack)
    {
        params.addHeader("appversion", String.valueOf(AppUtil.getVersionCode(ctx)));
        HttpManagerImpl mHttpManager = (HttpManagerImpl) x.http();

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callBack.onResponse(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                callBack.onFailure();
                if (ex instanceof SocketTimeoutException) {
                    Log.e("NetUtil","连接超时1");
                    DialogUtils.showToast(ctx,R.string.connect_time_out);
                }

                if (ex instanceof ConnectException) {
                    Log.e("NetUtil","连接超时2");
                    //song 加载结束取消加载框
//                    DialogUtil.dismissProgressDialog();
                    DialogUtils.showToast(ctx,R.string.connect_time_out);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                callBack.onFailure();
                Log.e("NetUtil","onCancelled has  exception ： "+ cex.getMessage());
            }

            @Override
            public void onFinished() {
                //song 加载结束取消加载框
                DialogUtils.dismissProgressDialog();
                callBack.onFailure();
                Log.e("NetUtil","onFinished" );
            }
        });
    }




    public static byte[] sendDate(String address, HashMap<String,String> input) {
//        HttpURLConnection conn = null;
        byte[] response = null;

        HttpClient httpCient = new DefaultHttpClient();
                         //第二步：创建代表请求的对象,参数是访问的服务器地址
        HttpPost httpGet = new HttpPost(address);
        for(String key: input.keySet())
        {
            String value = input.get(key);
            httpGet.addHeader(key,value);
        }
        try {
               //第三步：执行请求，获取服务器发还的相应对象
                HttpResponse httpResponse = httpCient.execute(httpGet);
                //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = httpResponse.getEntity();
                Header[] mHeadAll = httpResponse.getAllHeaders();
                for(int i=0;i<mHeadAll.length;i++)
                {
                    Header mItemHeader = mHeadAll[i];
                    String key = mItemHeader.getName();
                    if(key.equals("servertime"))
                    {
                        String strDate = mItemHeader.getValue();
                        Long timestamp = Long.parseLong(strDate) * 1000;
                        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd H H:mm:ss");
                        TimeZone tz = TimeZone.getDefault();
                        String strTz = tz.getDisplayName(false, TimeZone.SHORT);
                        sdf.setTimeZone(TimeZone.getTimeZone(strTz));
                        String snow = sdf.format(new Date(timestamp));
                    }
                    Log.i("Maintenance","heads-> key:" + mItemHeader.getName() + "\tvalue: "+ mItemHeader.getValue());
                }
                        InputStream in =  entity.getContent();
                InputStreamReader isr= new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                        byte[] temp = new byte[1024];
                        String str = null;
                        StringBuilder sb = new StringBuilder();
                        while ((str = br.readLine())!= null)
                        {
                            sb.append(str);
                        }
                        in.close();
//                        String responses = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串
                        Log.i("Maintenance","" + sb.toString());
                        //在子线程中将Message对象发出去
//                                        Message message = new Message();
//                                        message.what = SHOW_RESPONSE;
//                                       message.obj = response.toString();
//                                         handler.sendMessage(message);
                     }

            } catch (Exception e) {
            // TODO Auto-generated catch block
                e.printStackTrace();
            }

//        if(address!= null)
//        {
//            try {
//				/*乱码转化
//				String url = URLEncoder.encode(address,"utf-8");*/
//
//
//
//                conn = getConnection(address);
//                conn.setConnectTimeout(6 * 1000);
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("content-type",
//                        "application/octet-stream");
//
//                if (input != null && input.length > 0) {
//                    //发送数据加密
//                    OutputStream os = conn.getOutputStream();
//                    os.write(input);
//                    os.close();
//                }
//
//                int code = conn.getResponseCode();
//
//                System.out.println("-->class NetTool method sendData the reponse code :" + code);
//                if (code == HttpURLConnection.HTTP_OK) {
//                    InputStream is = conn.getInputStream();
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    byte[] data = new byte[1024];
//                    int len = -1;
//                    while ((len = is.read(data)) != -1) {
//                        bos.write(data, 0, len);
//                    }
//                    response = bos.toByteArray();
//                }
//            }catch(Exception e)
//            {
//
//            }finally {
//                if (conn != null) {
//                    conn.disconnect();
//                }
//            }
//        }
        return response;
    }

    /**
     * 判断接入点方式（net/wap），来建立连接
     *
     * @param address
     * @return
     * @throws Exception
     */
    private static HttpURLConnection getConnection(String address) throws Exception {
        HttpURLConnection conn = null;

        @SuppressWarnings("deprecation")
        String proxyHost = android.net.Proxy.getDefaultHost();
        if (proxyHost != null) {
            // wap方式，要加网关
            @SuppressWarnings("deprecation")
            java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new InetSocketAddress(android.net.Proxy.getDefaultHost(),
                            android.net.Proxy.getDefaultPort()));
            conn = (HttpURLConnection) new URL(address).openConnection(p);
        } else {
            conn = (HttpURLConnection) new URL(address).openConnection();
        }
        return conn;
    }

    public final static String HTTP_METHOD_GET = "GET";
    public final static String HTTP_METHOD_POST = "POST";
    public final static String HTTP_METHOD_PUT = "PUT";
    public final static String HTTP_METHOD_DELETE = "DELETE";

    public static interface OnSendListener {
        public void onGetResponse(Object response);

        public void onNetOperationFail();

        public void onNetError();
    }

}
