package cn.rongcloud.im.ui.fragment;

import android.view.View;
import android.widget.TextView;

import java.util.List;

import io.rong.imkit.fragment.ConversationListFragment;

public class ConversationListSaFragment extends ConversationListFragment {


    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        TextView textView = new TextView(this.getActivity());
        textView.setText("1111");
        headerViews.add(textView);
        return headerViews;
    }
}
