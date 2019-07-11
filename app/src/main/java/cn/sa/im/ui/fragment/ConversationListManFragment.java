package cn.sa.im.ui.fragment;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class ConversationListManFragment extends ConversationListFragment {


    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        TextView textView = new TextView(this.getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText("这是添加的头部布局");
        headerViews.add(textView);
        return headerViews;
    }


    @Override
    public void getConversationList(Conversation.ConversationType[] conversationTypes, final IHistoryDataResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                //在这里移除掉不想要的会话。
                conversations.remove(0);
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                if (callback != null) {
                    List<Conversation> resultConversations = new ArrayList<>();
                    if (conversations != null) {
                        for (Conversation conversation : conversations) {
                            if (!shouldFilterConversation(conversation.getConversationType(), conversation.getTargetId())) {
                                resultConversations.add(conversation);
                            }

                        }
                    }
                    callback.onResult(resultConversations);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                if (callback != null) {
                    callback.onError();
                }
            }
        }, conversationTypes);
    }
}