package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.im.App;
import cn.rongcloud.im.MainActivity;
import cn.rongcloud.im.R;
import cn.rongcloud.im.util.RongGenerate;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class SplashActivity extends Activity {
    private Context context;
    private android.os.Handler handler = new android.os.Handler();
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        context = this;
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        connect("2EWeL2nBdlnGMY6EYeLtfXHte7+VrAhsnSjOcYQ3SKP0EHUSc/FnS8sMXLFmvizg0dsYRNvxZh46EY0DeEjyZg==");
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void connect(String token) {

        //if (getApplicationInfo().packageName.equals(App.getCurProcessName(getApplicationContext()))) {

        RongIM.connect(token, new RongIMClient.ConnectCallback() {

            /**
             * Token 错误。可以从下面两点检查 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             *                  2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            @Override
            public void onTokenIncorrect() {

            }

            /**
             * 连接融云成功
             * @param userid 当前 token 对应的用户 id
             */
            @Override
            public void onSuccess(String userid) {
                Log.d("TAG", "--onSuccess" + userid);
                //参数设置为 true，由 IMKit 来缓存用户信息。
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
                            @Override
                            public UserInfo getUserInfo(String s) {
                                return findUserById(s);
                            }
                        }, true);
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                });

                //startActivity(new Intent(SplashActivity.this, ConversationListActivity.class));
                //finish();
            }
            /**
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.d("TAG", "--onSuccess" + errorCode);
            }
        });
        //}
    }

    //网络图片 没缓存 如果需要请求的图片要缓存。要刷新。
    private UserInfo findUserById(String s) {

//        switch (s){
//            case "10001":
//                return new UserInfo("userId", "大鱼", Uri.parse("https://www.baidu.com/img/bd_logo1.png"));
//            case "10002":
//                return new UserInfo("userId", "双鱼", Uri.parse("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3709603467,2914885303&fm=58&bpow=512&bpoh=512"));
//            case "10003":
//                return new UserInfo("userId", "三鱼", Uri.parse("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=2967361950,4205462985&fm=58&bpow=802&bpoh=652"));
//        }
        switch (s) {
            case "10001":
                name = "大鱼";
                break;
            case "10002":
                name = "双鱼";
                break;
            case "10003":
                name = "三鱼";
                break;
            default:
                name = "鱼群";
                break;
        }
        Uri portrait = Uri.parse(RongGenerate.generateDefaultAvatar(name, s));
        return new UserInfo(s, name, portrait);
    }
}
