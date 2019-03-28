package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import cn.rongcloud.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.Event;

public class MeetActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        return super.onKeyDown(keyCode, event);
    }
}
