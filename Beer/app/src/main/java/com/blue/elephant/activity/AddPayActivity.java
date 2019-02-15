package com.blue.elephant.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.blue.elephant.R;
import com.blue.elephant.util.CallBack;
import com.blue.elephant.util.ContentPath;
import com.blue.elephant.util.DialogUtils;
import com.blue.elephant.util.NetUtil;
import com.blue.elephant.util.QuickLoadCallBack;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

public class AddPayActivity extends BaseActivity {

    private CardInputWidget mCardView;
    private Card cardToSave;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pay);
        mCardView = findViewById(R.id.add_pay_card);
        ImageView actionBarLeft = findViewById(R.id.actionbar_left);
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        TextView mSubmitView = findViewById(R.id.add_pay_submit);
        actionBarLeft.setImageResource(R.mipmap.back_arraw);
        actionBarTitle.setText(R.string.add_pay_title);

        actionBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPayActivity.this.finish();
            }
        });

        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCard();
            }
        });

    }


    private void requestCard()
    {
        cardToSave = mCardView.getCard();
        if(cardToSave == null)
        {
            // Do not continue token creation.
            Log.i("AddPay","卡 创建失败！");
            return ;
        }

        if (!cardToSave.validateCard()) {

            //提示验证失败！
            Log.i("AddPay","验证失败！");
            return;
        }
        String message = getResources().getString(R.string.bill_list_load);
        DialogUtils.showProgressDialog(AddPayActivity.this, message);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /***
                 * 但是，您创建Card对象后，现在可以继续并将其转换为令牌。 您不应该在本地存储对象。
                 */
                Stripe stripe = new Stripe(AddPayActivity.this, ContentPath.card_public_key);
                stripe.createToken(
                        cardToSave,
                        new TokenCallback() {
                            public void onSuccess(final Token token) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.dismissProgressDialog();
                                    }
                                });
                                // Send token to your server
                                if(token == null)
                                {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DialogUtils.showToast(AddPayActivity.this,R.string.bill_add_error);
                                        }
                                    });
                                    return ;
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        addCard(token.getId());
                                    }
                                });
//                                Log.i("AddPay","token id :" + token.getId() + "\t type:" + token.getType() + "\t backAccount:"
//                                + token.getBankAccount() + "\t card:"+ token.getCard() + "\tcreate:" + token.getCreated()
//                                        + "\t live:"+ token.getLivemode()
//                                + "\tuser:" + token.getUsed());

                            }
                            public void onError(Exception error) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.dismissProgressDialog();
                                    }
                                });
                                DialogUtils.showToast(AddPayActivity.this,error.getMessage());
                            }
                        }
                );
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();
    }



    private void addCard(final String token)
    {
        if (!NetUtil.hasNet(AddPayActivity.this)) {
            DialogUtils.showToast(AddPayActivity.this,R.string.check_network_connect);
            return;
        }
        String url = ContentPath.bindCard ;
        RequestParams params = new RequestParams(url);
        params.addBodyParameter("token", token);
        String message = getResources().getString(R.string.bill_list_load);
        DialogUtils.showProgressDialog(AddPayActivity.this, message);
        onConnect(params, new CallBack() {
            @Override
            public void onResponse(String response) {
                DialogUtils.dismissProgressDialog();
//                Log.i("Bill","" + response);
                try{
                    JSONObject mResponseObject = new JSONObject(response);
                    String status = mResponseObject.optString("status");
                    if(status.equals("1"))
                    {
//                        Log.i("Bill","" + response);
                        AddPayActivity.this.finish();

                    }
                    else if(status.equals("0"))
                    {
                        String errorCode =  mResponseObject.optString("errorcode");
                        if(errorCode.equals("60001"))
                        {
                            quickLogin(new QuickLoadCallBack() {
                                @Override
                                public void doSomeThing(Boolean boolon) {
                                    if (boolon == true) {
                                        addCard(token);
                                    } else {
                                        reLogin(AddPayActivity.this);
                                    }
                                }
                            });
                        }else
                        {
                            String msg = mResponseObject.optString("msg");
                            DialogUtils.showToast(AddPayActivity.this,msg);
                        }
                    }
                    mResponseObject.optJSONObject("result");
                }catch(Exception e)
                {
                    Log.e("RentOrder","" + e.getMessage());
                }


            }

            @Override
            public void onFailure() {
                DialogUtils.dismissProgressDialog();
            }
        });
    }

}
