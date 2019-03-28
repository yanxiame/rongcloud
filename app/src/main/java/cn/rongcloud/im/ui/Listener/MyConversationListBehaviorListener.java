package cn.rongcloud.im.ui.Listener;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.model.Conversation;

public class MyConversationListBehaviorListener implements RongIM.ConversationListBehaviorListener {

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {

        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {

        return false;
    }

    @Override
    public boolean onConversationLongClick(final Context context, View view, UIConversation uiConversation) {
        uiConversation.addNickname("10002");
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);

        final View textEntryView = factory.inflate(R.layout.dialog, null);
        TextView resultOne=(TextView)textEntryView.findViewById(R.id.resultOne); //resultone is a textview in xml dialog



        alert.setView(textEntryView);
        final AlertDialog dialog=alert.show();
        textEntryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return true;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {

        return false;
    }
}
