package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.sa.im.R;
import cn.sa.im.ui.activity.MeetActivity;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongKitIntent;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

@ProviderTag(
        messageContent = ApkMessage.class,
        showReadState = true
)
public class ApkItemProvider extends IContainerItemProvider.MessageProvider<ApkMessage> {

    public ApkItemProvider() {
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_apk_message, null);
        ViewHolder holder = new ViewHolder();
        holder.trans_bg = (RelativeLayout) view.findViewById(R.id.rl_trans_bg);
        holder.tvTitle = (TextView) view.findViewById(R.id.tv_trans_tip);
        holder.tvStoreName = (TextView) view.findViewById(R.id.tv_trans_money);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, ApkMessage apkMessage, UIMessage message) {

        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();

        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.trans_bg.setBackgroundResource(R.drawable.jrmf_trans_bg_to);
        } else {
            holder.trans_bg.setBackgroundResource(R.drawable.jrmf_trans_bg_from);
        }
        Log.i("TAG", apkMessage.getExtra() + "}}}}}}");
        Log.i("TAG", message.getExtra() + "@@@@@@@");
        Log.i("TAG", message.getMessage().getExtra() + "#######");

        if (message.getExtra() != null && !message.getExtra().equals("")) {
            if ("isopen".equals(message.getExtra())) {
                holder.tvTitle.setText("¥1.00");
                holder.tvStoreName.setText("已经领取");
            } else {
                holder.tvTitle.setText("¥1.00");
                holder.tvStoreName.setText("未领取");
            }
        } else {
            holder.tvTitle.setText("¥1.00");
            Log.i("TAG","!!!!!2"+apkMessage.getPhoneNum());
            Log.i("TAG","!!!!!3"+apkMessage.getIsReceived());
            holder.tvStoreName.setText(apkMessage.getPhoneNum());
        }

    }

    @Override
    public Spannable getContentSummary(ApkMessage redPackageMessage) {
        return null;
    }

    @Override
    public void onItemClick(View view, int i, ApkMessage redPackageMessage, UIMessage uiMessage) {
        Intent intent = new Intent("cn.sa.im.intent.action.openmeet");
        intent.setPackage(view.getContext().getPackageName());
        intent.putExtra("isopen", redPackageMessage.getExtra());
        intent.putExtra("direction", uiMessage.getMessageDirection().getValue());
        intent.putExtra("conversationtype", uiMessage.getConversationType());
        intent.putExtra("targid", uiMessage.getTargetId());
        intent.putExtra("sendid", uiMessage.getSenderUserId());
        intent.putExtra("uimessage", uiMessage.getMessage());
        view.getContext().startActivity(intent);
    }

    @Override
    public void onItemLongClick(final View view, int i, ApkMessage redPackageMessage, UIMessage uiMessage) {

    }

    private static class ViewHolder {
        TextView tvTitle, tvStoreName;
        RelativeLayout trans_bg;
    }
}
