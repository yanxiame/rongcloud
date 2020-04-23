package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.util.Locale;

import cn.sa.im.util.RongGenerate;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

public class StickerSendMessageTask {

    private static String sTargetId;
    private static Conversation.ConversationType sConversationType;
    private static final String FORMAT = "[%s]"; // 推送格式

    public static void config(String targetId, Conversation.ConversationType conversationType) {
        sTargetId = targetId;
        sConversationType = conversationType;
    }

    public static void sendMessage(Context context, int res) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
        Uri uri= Uri.parse(RongGenerate.generate(bitmap,String.valueOf(res),"1"));
        ImageMessage imageMessage = ImageMessage.obtain(uri,uri);

        Message message=Message.obtain(sTargetId, Conversation.ConversationType.PRIVATE,imageMessage);

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

}
