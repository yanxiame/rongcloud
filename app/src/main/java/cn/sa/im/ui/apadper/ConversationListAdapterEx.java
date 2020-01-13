package cn.sa.im.ui.apadper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import cn.sa.im.R;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

public class ConversationListAdapterEx extends ConversationListAdapter {
    public ConversationListAdapterEx(Context context) {
        super(context);
    }

    LinearLayout leftImageLayout;
    boolean isShowCheckbox;

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
        View view =super.newView(context, position, group);
        leftImageLayout = findViewById(view, io.rong.imkit.R.id.ll_conversation_check);
        return view;
    }

    @Override
    protected void bindView(View v, int position, UIConversation data) {
        if (data != null) {
            if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION))
                data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
        }
        Log.i("TAG","2222"+isShowCheckbox);
        if(isShowCheckbox()) {leftImageLayout.setVisibility(View.VISIBLE);}
        super.bindView(v, position, data);

    }


    public boolean isShowCheckbox() {
        return isShowCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox) {
        isShowCheckbox = showCheckbox;
    }
}
