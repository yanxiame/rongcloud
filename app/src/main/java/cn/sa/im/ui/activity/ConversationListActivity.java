package cn.sa.im.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;
import java.util.Map;

import cn.sa.im.R;
import cn.sa.im.ui.widget.BottomBar;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

public class ConversationListActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversationlist);

    }

}