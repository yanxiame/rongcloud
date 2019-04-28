package cn.rongcloud.im.ui.widget.plugin;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

@ProviderTag(
        messageContent = InsertMessage.class,
        showReadState = true
)
public class InsertItemProvider extends IContainerItemProvider.MessageProvider<InsertMessage> {

    public InsertItemProvider() {
    }
    private Context context;
    String[] city = {"了解哪里的房价，深圳？","了解哪里的房价，北京？","了解哪里的房价，上海？","了解哪里的房价，香港？"} ;


    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        //这就是展示在会话界面的自定义的消息的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_insert_message, null);
        ViewHolder holder = new ViewHolder();
        holder.listview = (ListView) view.findViewById(R.id.lv_messsage);
        this.context=context;
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, InsertMessage insertMessage, UIMessage message) {
        //根据需求，适配数据
        ViewHolder holder = (ViewHolder) view.getTag();
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            //holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            //holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }
        holder.listview.setAdapter(new InsertAdapter(context, city));

    }

    @Override
    public Spannable getContentSummary(InsertMessage redPackageMessage) {
        return null;
    }

    @Override
    public void onItemClick(View view, int i, InsertMessage redPackageMessage, UIMessage uiMessage) {
        Log.i("TAG","TAG"+uiMessage.getMessageId());
        TextMessage textMessage=TextMessage.obtain(city[i]);
        Message.ReceivedStatus receivedStatus = new Message.ReceivedStatus(1);


//        RongIM.getInstance().sendmessage(Conversation.ConversationType.PRIVATE, uiMessage.getTargetId(), uiMessage.getSenderUserId(), receivedStatus, textMessage, new RongIMClient.ResultCallback<Message>() {
//            @Override
//            public void onSuccess(Message message) {
//                Log.i("TAG","1");
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//                Log.i("TAG","2");
//            }
//        });
    }

    @Override
    public void onItemLongClick(final View view, int i, InsertMessage redPackageMessage, UIMessage uiMessage) {


    }

    private class ViewHolder {
        ListView listview;
    }
    private class InsertAdapter<T> extends BaseAdapter{

        Context context;
        private List<T> mObjects;
        public InsertAdapter(Context context,T[] objects){
            this.context=context;
            this.mObjects= Arrays.asList(objects);
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }

        @Override
        public Object getItem(int position) {
            return mObjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view =  LayoutInflater.from(context).inflate(R.layout.item_insert_message_text,null);
            TextView textView=view.findViewById(R.id.item_insert_message);

            textView.setText(mObjects.get(position).toString());

            return view;
        }
    }

}
