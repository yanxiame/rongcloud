package cn.sa.im.ui.fragment;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import io.rong.imkit.fragment.ConversationListFragment;

public class ConversationListWoManFragment extends ConversationListFragment {


    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        TextView textView = new TextView(this.getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText("这是添加的头部布局");
        headerViews.add(textView);
        return headerViews;
    }
}
