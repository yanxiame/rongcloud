package cn.sa.im.ui.apadper;

import android.view.View;

import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imkit.widget.provider.PrivateConversationProvider;
@ConversationProviderTag(conversationType = "private", portraitPosition = 1)
public class PrivateConversationProviderEx extends PrivateConversationProvider {
    @Override
    public void bindView(View v, int position, UIConversation data) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        super.bindView(v, position, data);
        holder.readStatus.setVisibility(View.GONE);
    }
}
