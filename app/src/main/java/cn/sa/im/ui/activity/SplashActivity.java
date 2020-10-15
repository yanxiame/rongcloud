package cn.sa.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import java.util.HashMap;
import java.util.Map;

import cn.sa.im.App;
import cn.sa.im.MainActivity;
import cn.sa.im.R;
import cn.sa.im.util.RongGenerate;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

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

        // 100//03
        connect("qkG1qvBx5GN2rRWp3lUGLl2lXYyV+kg2@9qrf.sg.rongnav.com;9qrf.sg.rongcfg.com");
        // 10001
        //connect("pGJotIrFCum8Ft/A0OOYJvMFxrVBB+C6hCY4WZALhv4=@vvrh.cn.rongnav.com;vvrh.cn.rongcfg.com");

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
                        //sendMessage();
                        RongIMClient.getInstance().cleanHistoryMessages(Conversation.ConversationType.PRIVATE,"10003",System.currentTimeMillis(),true,null);

                        RongIM.setGroupInfoProvider(new RongIM.GroupInfoProvider() {
                            @Override
                            public Group getGroupInfo(String s) {
                                return new Group(s,"群聊",Uri.parse("https://www.baidu.com/img/bd_logo1.png"));
                            }
                        },true);
                        //RongIM.getInstance().refreshUserInfoCache();
                        //RongIM.getInstance().refreshGroupInfoCache();
                        RongIM.setGroupUserInfoProvider(new RongIM.GroupUserInfoProvider() {
                            @Override
                            public GroupUserInfo getGroupUserInfo(String s, String s1) {

                                return null;
                            }
                        },true);
                        //RongIM.getInstance().refreshGroupUserInfoCache();

                        RongIM.setUserInfoProvider(new RongIM.UserInfoProvider() {
                            @Override
                            public UserInfo getUserInfo(String s) {
                                Log.i("TAG",s);
                                return findUserById(s);
                            }
                        }, false);


                        Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;
                        String targetId = "10001";
                        // 消息免打扰
                        Conversation.ConversationNotificationStatus notificationStatus = Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;



                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }

                });
                //sendMessage();
                insertMessage();
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
    public void insertMessage(){
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.rc_loading);
        Uri uri= Uri.parse(RongGenerate.generate(bitmap,name,"1"));
        Log.i("TAG",RongGenerate.generate(bitmap,name,"1"));
//        Uri uri1=Uri.parse("file:///storage/emulated/0/Pictures/WeiXin/mmexport1597074491083.jpg");
        Uri uri1=Uri.parse("file:///storage/emulated/0/DCIM/Alipay/1597620368962.jpg");
        ImageMessage imageMessage = ImageMessage.obtain(uri1,uri1);
        Message message=Message.obtain("rtcu013", Conversation.ConversationType.PRIVATE,imageMessage);

        RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE,"rtcu013",RongIM.getInstance().getCurrentUserId(), new Message.ReceivedStatus(0),imageMessage,null);

    }

    private void sendMessage() {
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.rc_loading);
        Uri uri= Uri.parse(RongGenerate.generate(bitmap,name,"1"));
        Log.i("TAG",RongGenerate.generate(bitmap,name,"1"));
//        Uri uri1=Uri.parse("file:///storage/emulated/0/Pictures/WeiXin/mmexport1597074491083.jpg");

        Uri uri1=Uri.parse("file:///storage/emulated/0/DCIM/Alipay/1597620368962.jpg");
        ImageMessage imageMessage = ImageMessage.obtain(uri1,uri1);
        Message message=Message.obtain("rtcu013", Conversation.ConversationType.PRIVATE,imageMessage);

        RongIM.getInstance().sendImageMessage(message, "", "", new RongIMClient.SendImageMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

            }

            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onProgress(Message message, int i) {

            }
        });
    }

    //网络图片 没缓存 如果需要请求的图片要缓存。要刷新。
    private UserInfo findUserById(String s) {

        switch (s){
            case "10001":
                return new UserInfo(s, "大鱼", Uri.parse("https://www.baidu.com/img/bd_logo1.png"));
            case "10002":
                return new UserInfo(s, "双鱼", Uri.parse("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3709603467,2914885303&fm=58&bpow=512&bpoh=512"));
            case "10003":
                return new UserInfo(s, "三鱼", Uri.parse("https://ss0.baidu.com/6ONW  sjip0QIZ8tyhnq/it/u=2967361950,4205462985&fm=58&bpow=802&bpoh=652"));
        }
        return new UserInfo(s,this.getResources().getString(R.string.app_name),Uri.parse("https://dss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2464709569,1785793044&fm=26&gp=0.jpg"));
//        switch (s) {
//            case "10001":
//                name = "大鱼";
//                break;
//            case "10002":
//                name = "双鱼";
//                break;
//            case "10003":
//                name = "三鱼";
//                break;
//            default:
//                name = "鱼群";
//                break;
//        }
//        Uri portrait = Uri.parse(RongGenerate.generateDefaultAvatar(name, s));
////        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.rc_loading);
////        Uri uri= Uri.parse(RongGenerate.generate(bitmap,name,s));
//        return new UserInfo(s, name, portrait);
    }
}
