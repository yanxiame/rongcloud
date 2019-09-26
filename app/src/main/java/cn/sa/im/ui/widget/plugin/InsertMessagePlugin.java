package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sa.im.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class InsertMessagePlugin implements IPluginModule {

    private Context context;
    private Conversation.ConversationType conversationType;
    private String targetId;

    @Override
    public Drawable obtainDrawable(Context context) {

        return context.getResources().getDrawable(R.drawable.u261d);
    }

    @Override
    public String obtainTitle(Context context) {
        return "插入消息";
    }

    @Override
    public void onClick(final Fragment fragment, RongExtension rongExtension) {
        Log.i("TAG","222222");




//        Toast.makeText(rongExtension.getContext(), "发送一条自定义消息", Toast.LENGTH_LONG).show();
//        Uri uri = Uri.parse("file://https://ss3.bdstatic.com/yrwDcj7w0QhBkMak8IuT_XF5ehU5bvGh7c50/logopic/76a0982ed6098fc725849ece8b97e5c4_fullsize.jpg@s_1,w_484,h_484");
//        ImageMessage imageMessage = ImageMessage.obtain(uri,uri,true);
//
//        RongIM.getInstance().sendImageMessage(Conversation.ConversationType.PRIVATE, rongExtension.getTargetId(), imageMessage, null, null, new RongIMClient.SendImageMessageCallback() {
//                        @Override
//                        public void onAttached(Message message) {
//                            Log.i("TAG","8");
//                        }
//
//                        @Override
//                        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
//                                Log.i("TAG","1"+errorCode.getMessage());
//                            }
//
//                        @Override
//                        public void onSuccess(Message message) {
//                            Log.i("TAG","2");
//                            }
//
//                        @Override
//                        public void onProgress(Message message, int i) {
//                            Log.i("TAG","8");
//                        }
//                    });
//

//        final InsertMessage insertMessage =new InsertMessage();
//        Map<String,String> map=new HashMap<>();
//        map.put("1","1");
//        insertMessage.setMapmessage(map);
//        Message message = Message.obtain(rongExtension.getTargetId(), rongExtension.getConversationType(),insertMessage);
//        RongIM.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
//
//            @Override
//            public void onAttached(Message message) {
//
//            }
//
//            @Override
//            public void onSuccess(Message message) {
////                if(message.getContent() instanceof ApkMessage){
////
////                    ApkMessage msg = (ApkMessage)message.getContent();
////
////                    msg.setExtra("1");
////
////                }
//
//            }
//
//            @Override
//            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
//                Log.i("TAG!!!",message.getContent()+"!"+errorCode.getMessage());
//            }
//        });
    }
    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

        Log.i("TAG","1asdf");

    }
}
