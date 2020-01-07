package cn.sa.im.ui.apadper;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.adapter.MessageListAdapter;

public class MessageListAdapterEx extends MessageListAdapter {
    public MessageListAdapterEx(Context context) {
        super(context);
    }

    @Override
    protected void bindView(View v, int position, UIMessage data) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        super.bindView(v, position, data);
        holder.nameView.setTextColor(Color.RED);
        //holder.nameView.setVisibility(View.GONE);
        //holder.leftIconView.setVisibility(View.GONE);
        //holder.rightIconView.setVisibility(View.GONE);
    }
}
