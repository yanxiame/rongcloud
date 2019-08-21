package cn.sa.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.widget.AutoRefreshListView;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

import static android.content.Context.MODE_PRIVATE;

/**
 * 聊天记录
 *
 */
public class ConversationSaFragment extends ConversationFragment {

    @Override
    protected void initFragment(Uri uri) {
        super.initFragment(uri);

    }

    @Override
    public void onSendToggleClick(View v, String text) {
        Log.i("TAG","1");
        SharedPreferences sp = getActivity().getSharedPreferences("**", MODE_PRIVATE);
    }
}

