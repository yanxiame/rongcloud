package cn.sa.im.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import io.rong.imkit.utils.RongLinkify;
import io.rong.imkit.widget.AutoLinkTextView;

public class TestTextView extends AutoLinkTextView {
        public TestTextView(Context context) {
            super(context);
        }

        public TestTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public TestTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public TestTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setAutoLinkMask(0);
            super.setText(text, type);
            RongLinkify.addLinks(this, RongLinkify.ALL);
        }

}
