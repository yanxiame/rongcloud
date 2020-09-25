package cn.sa.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.Locale;

import cn.sa.im.R;
import cn.sa.im.ui.fragment.ConversationSaFragment;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.model.Conversation;

public class ConversationDynamicActivity extends FragmentActivity {
    private String mTargetId; //目标 Id
    private String mTargetIds;
    private Conversation.ConversationType mConversationType; //会话类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rong_activity);
        /* 从 intent 携带的数据里获取 targetId 和会话类型*/
        Intent intent = getIntent();
        mTargetId = intent.getData().getQueryParameter("targetId");
        mTargetIds = intent.getData().getQueryParameter("targetIds");

        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase());

        /* 新建 ConversationFragment 实例，通过 setUri() 设置相关属性*/
        ConversationSaFragment fragment = new ConversationSaFragment();
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();
        fragment.setUri(uri);
        /* 加载 ConversationFragment */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();
    }
}