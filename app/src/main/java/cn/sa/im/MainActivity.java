package cn.sa.im;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import cn.sa.im.ui.fragment.ContactsFragment;
import cn.sa.im.ui.fragment.ConversationListManFragment;
import cn.sa.im.ui.fragment.ConversationListWoManFragment;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.push.common.RLog;
import io.rong.push.notification.PushNotificationMessage;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    ViewPager viewPager;
    BottomNavigationView navigation;
    private MenuItem menuItem;
    private ConversationListManFragment ManFragment;
    private ConversationListWoManFragment WomanFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        viewPager.addOnPageChangeListener(this);
        navigation.setSelectedItemId(R.id.navigation_home);
        ManFragment = new ConversationListManFragment();
        WomanFragment =new ConversationListWoManFragment();
        RongIM.getInstance().joinChatRoom("1", 0, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i("TAG", "");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.i("TAG", errorCode.getMessage());
            }
        });


        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话，该会话聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//设置群组会话，该会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")
                .build();
        ManFragment.setUri(uri);
        WomanFragment.setUri(uri);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        //hw推送
        if (getIntent() != null) {
            uploadPushEvent(getIntent());
        }
    }

    private void uploadPushEvent(Intent intent) {
        //通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
        if (intent.getData() != null && intent.getData().getScheme() != null
                && intent.getData().getScheme().equals("rong") && intent.getData().getQueryParameter("isFromPush") != null
                && intent.getData().getQueryParameter("isFromPush").equals("true")) {
            recordHWNotificationEvent(intent);
        }
    }

    public void recordHWNotificationEvent(Intent intent) {
        if (intent != null) {
            String options = intent.getStringExtra("options");
            if (!TextUtils.isEmpty(options)) {
                try {
                    JSONObject jsonObject = new JSONObject(options);
                    if (jsonObject.has("rc")) {
                        JSONObject rc = jsonObject.getJSONObject("rc");
                        String pushId = rc.optString("id");
                        Log.i("TAG", pushId);
                        if (TextUtils.isEmpty(pushId)) {
                            RLog.d("TAG", "pushId is empty,recordNotificationEvent is failure");
                            return;
                        }

                        String objectName = rc.optString("objectName");
                        String userId = rc.optString("tId");
                        String type = rc.optString("sourceType");
                        PushNotificationMessage.PushSourceType sourceType = PushNotificationMessage.PushSourceType.FROM_ADMIN;
                        if (!TextUtils.isEmpty(type)) {
                            sourceType = PushNotificationMessage.PushSourceType.values()[Integer.parseInt(type)];
                        }

                    }
                } catch (JSONException var9) {
                    var9.printStackTrace();
                }
            }

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        switch (itemId) {
            case R.id.navigation_home:
                viewPager.setCurrentItem(0);
                break;
            case R.id.navigation_dashboard:
                viewPager.setCurrentItem(1);
                break;
            case R.id.navigation_notifications:
                viewPager.setCurrentItem(2);
                break;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        menuItem = navigation.getMenu().getItem(i);
        menuItem.setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {


        private Fragment[] mFragments = new Fragment[]{ManFragment, new ContactsFragment(), WomanFragment};

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


}
