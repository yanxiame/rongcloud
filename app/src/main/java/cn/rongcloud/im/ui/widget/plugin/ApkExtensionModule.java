package cn.rongcloud.im.ui.widget.plugin;

import android.content.Intent;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imlib.model.Conversation;

public class ApkExtensionModule extends DefaultExtensionModule {

    private ApkPlugin myPlugin;
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModuleList = new ArrayList<>();
        IPluginModule image = new ImagePlugin();
        IPluginModule apk =new ApkPlugin();
        if (conversationType.equals(Conversation.ConversationType.GROUP) ||
                conversationType.equals(Conversation.ConversationType.DISCUSSION) ||
                conversationType.equals(Conversation.ConversationType.PRIVATE)) {
                    pluginModuleList.add(image);
                    pluginModuleList.add(apk);
        } else {
            pluginModuleList.add(image);
        }

        return pluginModuleList;
    }


}
