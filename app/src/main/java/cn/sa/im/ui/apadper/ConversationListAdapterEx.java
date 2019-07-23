package cn.sa.im.ui.apadper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

public class ConversationListAdapterEx extends ConversationListAdapter {
    public ConversationListAdapterEx(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return super.newView(context, position, group);
    }

    @Override
    protected void bindView(View v, int position, UIConversation data) {
        if (data != null) {
            if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION))
                data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
        }
        super.bindView(v, position, data);
    }
}
