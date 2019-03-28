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
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class SplashActivity extends Activity {
    private Context context;
    private android.os.Handler handler = new android.os.Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        context = this;
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        //if (!TextUtils.isEmpty(cacheToken)) {
        connect("wA3wFI6kSYbJjWfmuNGHu3xpRjANxKgfakOnYLFljI/OtA5W8boG66fwh+Andndnz8N8q00tQ6b+F83vgH7ejQ==");

        //} else {
        //    handler.postDelayed(new Runnable() {
        //        @Override
        //        public void run() {
        //            goToLogin();
        //        }
        //    }, 800);
        //}
        //startConversationList();
    }
    private void startConversationList() {
        Map<String, Boolean> map = new HashMap<>();
        map.put(Conversation.ConversationType.PRIVATE.getName(), true); // 会话列表需要显示私聊会话, 第二个参数 true 代表私聊会话需要聚合显示
        map.put(Conversation.ConversationType.GROUP.getName(), false);  // 会话列表需要显示群组会话, 第二个参数 false 代表群组会话不需要聚合显示

        RongIM.getInstance().startConversationList(this.getApplicationContext(), map);
    }

    private void goToMain() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(context, LoginActivity.class));
        finish();
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
                    Log.i("TAG","111111");
                }

                /**
                 * 连接融云成功
                 * @param userid 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userid) {
                    Log.d("TAG", "--onSuccess" + userid);
                    //参数设置为 true，由 IMKit 来缓存用户信息。
                    RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
                        @Override
                        public UserInfo getUserInfo(String s) {
                            return findUserById(s);
                        }
                    },false);
                    startActivity(new Intent(SplashActivity.this, ConversationListDynamicActivtiy.class));
                    finish();
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

    private UserInfo findUserById(String s) {
        switch (s){
            case "10001":
                return new UserInfo("userId", "大鱼", Uri.parse("https://www.baidu.com/img/bd_logo1.png"));
            case "10002":
                return new UserInfo("userId", "双鱼", Uri.parse("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3709603467,2914885303&fm=58&bpow=512&bpoh=512"));
            case "10003":
                return new UserInfo("userId", "三鱼", Uri.parse("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=2967361950,4205462985&fm=58&bpow=802&bpoh=652"));

        }
        return new UserInfo("userId", "多鱼", Uri.parse("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1066474367,396145276&fm=58&bpow=708&bpoh=708"));


    }


}
