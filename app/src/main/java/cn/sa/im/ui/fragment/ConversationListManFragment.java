package cn.sa.im.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sa.im.R;
import cn.sa.im.ui.apadper.ConversationListAdapterEx;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class ConversationListManFragment extends ConversationListFragment {

    @Override
    protected void initFragment(Uri uri) {
        super.initFragment(uri);
        Log.i("TAG",uri.toString());
        if(uri.toString().contains("subconversationlist")){
            Log.i("TAG","111113243");
        }
    }

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

    @Override
    public boolean onPortraitItemLongClick(View v, UIConversation data) {
        Log.i("TAG","onPortraitItemLongClick");
        return super.onPortraitItemLongClick(v, data);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("TAG","onItemLongClick");
        return super.onItemLongClick(parent, view, position, id);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UIConversation conversation= (UIConversation) parent.getAdapter().getItem(position);
        Log.i("TAG","onItemClick");
        super.onItemClick(parent, view, position, id);
    }
}