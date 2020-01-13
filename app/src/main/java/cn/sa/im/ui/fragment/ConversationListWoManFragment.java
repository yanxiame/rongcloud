package cn.sa.im.ui.fragment;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

import cn.sa.im.R;
import cn.sa.im.ui.apadper.ConversationListAdapterEx;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

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

        conversationListAdapterEx.notifyDataSetChanged();

        return true;
        //return super.onItemLongClick(parent, view, position, id);
    }


}
