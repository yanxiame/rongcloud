package cn.rongcloud.im;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.health.SystemHealthManager;
import android.util.Log;

import cn.rongcloud.im.ui.Listener.MyConversationClickListener;
import cn.rongcloud.im.ui.Listener.MyConversationListBehaviorListener;
import cn.rongcloud.im.ui.widget.plugin.ApkExtensionModule;
import cn.rongcloud.im.ui.widget.plugin.ApkItemProvider;
import cn.rongcloud.im.ui.widget.plugin.ApkMessage;
import cn.rongcloud.im.ui.widget.plugin.InsertItemProvider;
import cn.rongcloud.im.ui.widget.plugin.InsertMessage;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongNotificationManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.pushconfig.PushConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RongIM.init(this);

        //自定义消息
        RongIM.registerMessageTemplate(new ApkItemProvider());
        RongIM.registerMessageTemplate(new InsertItemProvider());
        RongIM.registerMessageType(ApkMessage.class);
        RongIM.registerMessageType(InsertMessage.class);

        RongExtensionManager.getInstance().unregisterExtensionModule(new DefaultExtensionModule());
        //自定义plugin
        RongExtensionManager.getInstance().registerExtensionModule(new ApkExtensionModule());
        //会话列表
        RongIM.getInstance().setConversationListBehaviorListener(new MyConversationListBehaviorListener());
        //会话界面
        RongIM.getInstance().setConversationClickListener(new MyConversationClickListener());

        //华为推送成功  小米推送没有 appid
        PushConfig config = new PushConfig.Builder()
                .enableHWPush(true)
                .enableFCM(true)
                .build();
        RongPushClient.setPushConfig(config);
        //使用消息携带用户信息
        RongIM.getInstance().setMessageAttachedUserInfo(false);
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                if(message.getConversationType()== Conversation.ConversationType.SYSTEM){
                    //做自己的操作  发送本地通知//
                    //Log.i("TAG",message.getContent().getSearchableWord().get(0));
                    //RongNotificationManager.getInstance().onReceiveMessageFromApp(message, 0);
                }
                return false;
            }
        });

    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
