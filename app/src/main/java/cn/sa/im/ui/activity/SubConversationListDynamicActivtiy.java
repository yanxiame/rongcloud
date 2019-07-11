package cn.sa.im.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import cn.sa.im.R;
import io.rong.imkit.fragment.SubConversationListFragment;

public class SubConversationListDynamicActivtiy extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rong_activity);


        SubConversationListFragment fragment = new SubConversationListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();
    }
}
