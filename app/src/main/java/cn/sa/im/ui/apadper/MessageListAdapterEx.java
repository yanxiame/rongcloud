package cn.sa.im.ui.apadper;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.model.Message;

public class MessageListAdapterEx extends MessageListAdapter {
    public MessageListAdapterEx(Context context) {
        super(context);
    }

    @Override
    protected void bindView(View v, int position, UIMessage data) {
        final ViewHolder holder = (ViewHolder) v.getTag();
        super.bindView(v, position, data);
        if(data.getMessageDirection() == Message.MessageDirection.SEND){
//            holder.nameView.setText(RongUserInfoManager.getInstance().getUserInfo(data.getTargetId()).getName());
            holder.nameView.setText("456");
        }else {
            holder.nameView.setText("123");
        }
        holder.nameView.setVisibility(View.VISIBLE);
        holder.nameView.setTextColor(Color.RED);
        //holder.leftIconView.setVisibility(View.GONE);
        //holder.rightIconView.setVisibility(View.GONE);
    }
}
