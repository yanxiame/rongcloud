package cn.sa.im.ui.apadper;

import android.graphics.Color;
import android.view.View;

import cn.sa.im.R;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
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
            holder.readStatus.setVisibility(View.GONE);
    //        会话列表的控件
            holder.title.setTextColor(Color.RED);
            SpannableStringBuilder style1 = new SpannableStringBuilder("text111111112345678765432345671");
            style1.setSpan(new ForegroundColorSpan(v.getContext().getColor(R.color.colorAccent)), 13, 19, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.content.setText(style1);
            holder.time.setTextColor(Color.RED);
        }
    }
