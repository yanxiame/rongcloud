package cn.sa.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.sa.im.R;

public class HintSideBar extends RelativeLayout implements SideBar.OnChooseLetterChangedListener {

    private TextView tv_hint;

    private SideBar.OnChooseLetterChangedListener onChooseLetterChangedListener;

    public HintSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_hint_side_bar, this);
        initView();
    }

    private void initView() {
        SideBar sideBar = (SideBar) findViewById(R.id.sideBar);
        tv_hint = (TextView) findViewById(R.id.tv_hint);
        sideBar.setOnTouchingLetterChangedListener(this);
    }

    @Override
    public void onChooseLetter(String s) {
        tv_hint.setText(s);
        tv_hint.setVisibility(VISIBLE);
        if (onChooseLetterChangedListener != null) {
            onChooseLetterChangedListener.onChooseLetter(s);
        }
    }

    @Override
    public void onNoChooseLetter() {
        tv_hint.setVisibility(INVISIBLE);
        if (onChooseLetterChangedListener != null) {
            onChooseLetterChangedListener.onNoChooseLetter();
        }
    }

    public void setOnChooseLetterChangedListener(SideBar.OnChooseLetterChangedListener onChooseLetterChangedListener) {
        this.onChooseLetterChangedListener = onChooseLetterChangedListener;
    }
}