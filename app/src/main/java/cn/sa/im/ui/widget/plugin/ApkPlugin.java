package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import cn.sa.im.ui.activity.TakingPicturesExActivity;
import cn.sa.im.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.CommandMessage;
import io.rong.message.InformationNotificationMessage;

public class ApkPlugin implements IPluginModule {

    private Context context;
    private Conversation.ConversationType conversationType;
    private String targetId;

    @Override
    public Drawable obtainDrawable(Context context) {

        return context.getResources().getDrawable(R.drawable.u261d);
    }

    @Override
    public String obtainTitle(Context context) {
        return "见一见";
    }

    @Override
    public void onClick(final Fragment fragment, RongExtension rongExtension) {
//        Toast.makeText(rongExtension.getContext(), "发送一条自定义消息", Toast.LENGTH_LONG).show();
//        final cn.sa.im.ui.widget.plugin.ApkMessage apkMessage = new cn.sa.im.ui.widget.plugin.ApkMessage();
//        apkMessage.setUserName("$111");
//        apkMessage.setPhoneNum("转账给您");
//        apkMessage.setExtra("noopen");
        CustomizeMessage apkMessage=CustomizeMessage.obtain(1,"1","1");
        //InformationNotificationMessage informationNotificationMessage = InformationNotificationMessage.obtain("无人工在线");
        Message message = Message.obtain(rongExtension.getTargetId(), rongExtension.getConversationType(), apkMessage);
        RongIM.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {

            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                Log.i("TAG",message.getMessageId()+"");
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

            }
        });



    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
