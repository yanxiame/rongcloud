package cn.rongcloud.im;

import android.view.View;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.UnknownMessageItemProvider;
import io.rong.imlib.model.MessageContent;

public class UnknownMessageItemProviderex extends UnknownMessageItemProvider {

    @Override
    public void bindView(View v, int position, MessageContent content, UIMessage message) {
        super.bindView(v, position, content, message);

    }
}
