package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.BottomBar;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

/**
 * 这个activity集成的会话列表
 */
public class ConversationListDynamicActivtiy extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rong_activity);

        ConversationListFragment fragment = new ConversationListFragment();
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "true") //设置私聊会话，该会话聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//设置群组会话，该会话非聚合显示
                .build();
        fragment.setUri(uri);  //设置 ConverssationListFragment 的显示属性

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();

        //添加一行
        MessageItemLongClickAction action = new MessageItemLongClickAction.Builder()
                .titleResId(R.string.sa_dialog_item_message_delete)
                .actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
                    @Override
                    public boolean onMessageItemLongClick(Context context, final UIMessage message) {
                        Message[] messages = new Message[1];
                        messages[0] = message.getMessage();

                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        LayoutInflater factory = LayoutInflater.from(context);

                        final View textEntryView = factory.inflate(R.layout.dialog_conversation, null);
                        TextView resultOne = (TextView) textEntryView.findViewById(R.id.resultOne); //resultone is a textview in xml dialog
                        TextView resultTwo = (TextView) textEntryView.findViewById(R.id.resultTwo); //resultone is a textview in xml dialog
                        TextView resultThree = (TextView) textEntryView.findViewById(R.id.resultThree); //resultone is a textview in xml dialog

                        alert.setView(textEntryView);
                        final AlertDialog dialog = alert.show();
                        resultOne.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessage("10001", message);
                                dialog.dismiss();
                            }
                        });
                        resultTwo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessage("10002", message);
                                dialog.dismiss();
                            }
                        });
                        resultThree.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMessage("10003", message);
                                dialog.dismiss();
                            }
                        });

                        return false;
                    }
                }).build();
        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(action, 1);
    }

    private void sendMessage(String s, UIMessage message) {
        MessageContent messageContent = message.getContent();
        if (message.getConversationType().equals(Conversation.ConversationType.PRIVATE) && messageContent instanceof TextMessage) {
            // 构造 TextMessage 实例
            TextMessage myTextMessage = TextMessage.obtain(((TextMessage) messageContent).getContent());

            /* 生成 Message 对象。
             * "7127" 为目标 Id。根据不同的 conversationType，可能是用户 Id、群组 Id 或聊天室 Id。
             * Conversation.ConversationType.PRIVATE 为私聊会话类型，根据需要，也可以传入其它会话类型，如群组。
             */
            Message myMessage = Message.obtain(s, Conversation.ConversationType.PRIVATE, myTextMessage);
            RongIM.getInstance().sendMessage(myMessage, null, null, new IRongCallback.ISendMessageCallback() {
                @Override
                public void onAttached(Message message) {
                    //消息本地数据库存储成功的回调
                    Toast.makeText(ConversationListDynamicActivtiy.this, "转发成功", Toast.LENGTH_LONG).show();
                    ;

                }

                @Override
                public void onSuccess(Message message) {
                    //消息通过网络发送成功的回调
                }

                @Override
                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    //消息发送失败的回调
                }
            });
        } else {
            Toast.makeText(ConversationListDynamicActivtiy.this, "只支持文本消息", Toast.LENGTH_LONG).show();
        }

    }
}