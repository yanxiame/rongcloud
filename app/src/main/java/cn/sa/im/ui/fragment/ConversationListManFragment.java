package cn.sa.im.ui.fragment;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sa.im.R;
import cn.sa.im.ui.apadper.ConversationListAdapterEx;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class ConversationListManFragment extends ConversationListFragment {


    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        //View view=View.inflate(getContext(), R.layout.item_apk_message, null);
        TextView textView = new TextView(this.getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText(getString(R.string.conversatinolist_top_title));
        headerViews.add(textView);
        return headerViews;
    }

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new ConversationListAdapterEx(context);
    }

}