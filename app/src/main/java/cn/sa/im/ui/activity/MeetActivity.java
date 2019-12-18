package cn.sa.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import cn.sa.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.Event;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class MeetActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        final Button button = findViewById(R.id.meet_btn);
        final Intent intent = getIntent();
        final Message uiMessage = intent.getParcelableExtra("uimessage");
        if (Message.MessageDirection.SEND.getValue() == intent.getIntExtra("direction", 1)) {
            button.setVisibility(View.GONE);
        } else if (!getIntent().getStringExtra("isopen").equals("isopen")) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final cn.sa.im.ui.widget.plugin.ApkMessage apkMessage = new cn.sa.im.ui.widget.plugin.ApkMessage();
                    apkMessage.setUserName("$111");
                    apkMessage.setPhoneNum("转账给--");
                    apkMessage.setIsReceived(1);
                    apkMessage.setExtra("isopen");
                    RongIM.getInstance().sendDirectionalMessage(Conversation.ConversationType.setValue(intent.getIntExtra("conversation", 1)), intent.getStringExtra("targid"), apkMessage, new String[]{intent.getStringExtra("sendid")}, "", "", null);
                    uiMessage.setExtra("isopen");
                    RongIMClient.getInstance().setMessageExtra(uiMessage.getMessageId(), "isopen", null);
                    RongContext.getInstance().getEventBus().post(uiMessage);
                    finish();

                }
            });
        } else {
            button.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        return super.onKeyDown(keyCode, event);
    }
}
