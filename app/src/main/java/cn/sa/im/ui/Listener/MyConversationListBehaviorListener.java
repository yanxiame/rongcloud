package cn.sa.im.ui.Listener;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import cn.sa.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class MyConversationListBehaviorListener implements RongIM.ConversationListBehaviorListener {

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {

        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {

        return false;
    }

    @Override
    public boolean onConversationLongClick(final Context context, View view, final UIConversation uiConversation) {
//        String[] items = new String[]{view.getContext().getResources().getString(R.string.rc_dialog_item_message_delete),"标记未读"};
//        /**
//         * newInstance() 初始化OptionsPopupDialog
//         * @param items弹出菜单功能选项
//         * setOptionsPopupDialogListener()设置点击弹出菜单的监听
//         * @param which表示点击的哪一个菜单项,与items的顺序一致
//         * show()显示pop dialog
//         */
//        OptionsPopupDialog.newInstance(view.getContext(), items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//
//            @Override
//            public void onOptionsItemClicked(int which) {
//                if (which == 0) {
//                    RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
//                        @Override
//                        public void onSuccess(List<Conversation> conversations) {
//                            Log.i("TAG",conversations.size()+"QQQQ");
//                        }
//
//                        @Override
//                        public void onError(RongIMClient.ErrorCode errorCode) {
//
//                        }
//                    });
//                    RongIM.getInstance().removeConversation(uiConversation.getConversationType(), uiConversation.getConversationTargetId());
//                    RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
//                        @Override
//                        public void onSuccess(List<Conversation> conversations) {
//                            Log.i("TAG",conversations.size()+"WWWW");
//                        }
//
//                        @Override
//                        public void onError(RongIMClient.ErrorCode errorCode) {
//
//                        }
//                    });
//                } else if(which == 1){
//                    final Message.ReceivedStatus receivedStatus=new Message.ReceivedStatus(0);
//                    RongIMClient.getInstance().getMessage(uiConversation.getLatestMessageId(), new RongIMClient.ResultCallback<Message>() {
//                        @Override
//                        public void onSuccess(Message message) {
//
//                            message.setReceivedStatus(receivedStatus);
//                        }
//
//                        @Override
//                        public void onError(RongIMClient.ErrorCode errorCode) {
//
//                        }
//                    });
//
//                    RongIMClient.getInstance().setMessageReceivedStatus(uiConversation.getLatestMessageId(), receivedStatus,null);
//                }
//            }
//        }).show();
//        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
//        LayoutInflater factory = LayoutInflater.from(context);
//
//        final View textEntryView = factory.inflate(R.layout.dialog, null);
//        TextView resultOne=(TextView)textEntryView.findViewById(R.id.resultOne); //resultone is a textview in xml dialog
//
//        alert.setView(textEntryView);
//        final AlertDialog dialog=alert.show();
//        textEntryView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        Log.i("TAG","11111111");
        return false;
    }
}
