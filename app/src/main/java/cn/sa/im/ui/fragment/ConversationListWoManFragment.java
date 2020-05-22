package cn.sa.im.ui.fragment;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

import cn.sa.im.MainActivity;
import cn.sa.im.R;
import cn.sa.im.ui.apadper.ConversationListAdapterEx;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

public class ConversationListWoManFragment extends ConversationListFragment {

    ConversationListAdapterEx conversationListAdapterEx;

    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        TextView textView = new TextView(this.getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText(getString(R.string.conversatinolist_top_title));
        headerViews.add(textView);
        return headerViews;
    }
    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        conversationListAdapterEx = new ConversationListAdapterEx(context);
        return conversationListAdapterEx;
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("TAG","onItemLongClick");
        conversationListAdapterEx.setShowCheckbox(true);
        //conversationListAdapterEx.clear();
        MainActivity activity= (MainActivity) getActivity();
        activity.reLoadFragView();
        conversationListAdapterEx.notifyDataSetChanged();

        return true;
        //return super.onItemLongClick(parent, view, position, id);
    }

    @Override
    public void getConversationList(Conversation.ConversationType[] conversationTypes, IHistoryDataResultCallback<List<Conversation>> callback, boolean isLoadMore) {
        super.getConversationList(conversationTypes, callback, isLoadMore);
    }

    @Override
    public boolean shouldFilterConversation(Conversation.ConversationType type, String targetId) {
        if(targetId!="10003"){
            return true;
        }
        return super.shouldFilterConversation(type, targetId);
    }
}
