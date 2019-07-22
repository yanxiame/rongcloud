package cn.sa.im.ui.fragment;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import cn.sa.im.R;
import io.rong.imkit.fragment.ConversationListFragment;

public class ConversationListWoManFragment extends ConversationListFragment {


    @Override
    protected List<View> onAddHeaderView() {
        List<View> headerViews = super.onAddHeaderView();
        TextView textView = new TextView(this.getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setText(getString(R.string.conversatinolist_top_title));
        headerViews.add(textView);
        return headerViews;
    }
}
