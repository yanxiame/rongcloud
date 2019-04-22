package cn.rongcloud.im;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.Uri;

import cn.rongcloud.im.ui.Listener.MyConversationClickListener;
import cn.rongcloud.im.ui.Listener.MyConversationListBehaviorListener;
import cn.rongcloud.im.ui.widget.plugin.ApkExtensionModule;
import cn.rongcloud.im.ui.widget.plugin.ApkItemProvider;
import cn.rongcloud.im.ui.widget.plugin.ApkMessage;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;
import io.rong.push.pushconfig.PushConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RongIM.init(this);
        RongIMClient.init(this);

        //自定义消息
        RongIM.registerMessageTemplate(new ApkItemProvider());
        RongIM.registerMessageType(ApkMessage.class);

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
        //git
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