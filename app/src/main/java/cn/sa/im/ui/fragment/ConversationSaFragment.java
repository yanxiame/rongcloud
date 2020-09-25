package cn.sa.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;
import java.util.Locale;

import cn.sa.im.R;
import cn.sa.im.ui.apadper.MessageListAdapterEx;
import cn.sa.im.ui.widget.plugin.OnItemClickListener;
import cn.sa.im.ui.widget.plugin.RongEmoticonTab;
import cn.sa.im.util.RongGenerate;
import io.rong.common.RLog;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.model.Event;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoRefreshListView;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

import static android.content.Context.MODE_PRIVATE;

/**
 * 聊天记录
 *
 */
public class ConversationSaFragment extends ConversationFragment{

    private RongExtension mRongExtension;
    private AutoRefreshListView mList;
    private String mTargetId;
    private Conversation.ConversationType mConversationType;
    private MessageItemLongClickAction messageItemLongClickAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);

    }
    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        //return super.onResolveAdapter(context);
        return new MessageListAdapterEx(context);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mRongExtension = (RongExtension) view.findViewById(io.rong.imkit.R.id.rc_extension);
        mList = (AutoRefreshListView) view.findViewById( io.rong.imkit.R.id.rc_list);
        mRongExtension.setTag("11");
        mList.setSelection(mList.getCount());
        super.onViewCreated(view, savedInstanceState);
        //View s=view.findViewById(R.id.rc_layout_msg_list);
        //s.setBackground(getResources().getDrawable(R.drawable.bg_conversation));
        messageItemLongClickAction = new MessageItemLongClickAction.Builder()
                .title("收藏")
                .actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
                    @Override
                    public boolean onMessageItemLongClick(Context context, UIMessage message) {

                        return true;
                    }
                }).build();
        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(messageItemLongClickAction);


    }
    @Override
    protected void initFragment(Uri uri) {
        String typeStr = uri.getLastPathSegment().toUpperCase(Locale.US);
        mConversationType = Conversation.ConversationType.valueOf(typeStr);
        mTargetId = uri.getQueryParameter("targetId");
        super.initFragment(uri);

    }
    @Override
    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
        super.onPluginToggleClick(v, extensionBoard);
    }

    @Override
    public void onSendToggleClick(View v, String text)
    {
        super.onSendToggleClick(v,text);
    }

    @Override
    public void getRemoteHistoryMessages(Conversation.ConversationType conversationType, String targetId, long dateTime, int reqCount, IHistoryDataResultCallback<List<Message>> callback) {
        super.getRemoteHistoryMessages(conversationType, targetId, dateTime, reqCount, callback);
    }

    @Override
    public void getHistoryMessage(Conversation.ConversationType conversationType, String targetId, int lastMessageId, int reqCount, LoadMessageDirection direction, final IHistoryDataResultCallback<List<Message>> callback) {
        super.getHistoryMessage(conversationType, targetId, lastMessageId, reqCount, direction, new IHistoryDataResultCallback<List<Message>>() {
            @Override
            public void onResult(List<Message> data) {
                // 在这里remove
                // data = filterMessageList(data)
                callback.onResult(data);
            }
            @Override
            public void onError() {


            }
        });
    }

    @Override
    public boolean showMoreClickItem() {
        return true;
    }

    @Override
    public void onDestroy() {
        RongMessageItemLongClickActionManager
                .getInstance().removeMessageItemLongClickAction(messageItemLongClickAction);

        super.onDestroy();
    }

    @Override
    public void onEventMainThread(Event.MessageDeleteEvent deleteEvent) {
        super.onEventMainThread(deleteEvent);
        for (int messageId : deleteEvent.getMessageIds()) {
            //RongIM.getInstance().deleteRemoteMessages();
        }
    }

}

