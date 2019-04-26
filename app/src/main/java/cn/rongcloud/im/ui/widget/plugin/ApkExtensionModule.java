package cn.rongcloud.im.ui.widget.plugin;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imkit.widget.provider.LocationPlugin;
import io.rong.imlib.model.Conversation;

public class ApkExtensionModule extends DefaultExtensionModule {

    private ApkPlugin myPlugin;
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModuleList = new ArrayList<>();
        IPluginModule image = new ImagePlugin();
        IPluginModule apk =new ApkPlugin();
        IPluginModule insert =new InsertMessagePlugin();
        IPluginModule file =new FilePlugin();
        LocationPlugin locationPlugin =new LocationPlugin();
        if (conversationType.equals(Conversation.ConversationType.GROUP) ||
                conversationType.equals(Conversation.ConversationType.DISCUSSION) ||
                conversationType.equals(Conversation.ConversationType.PRIVATE)) {
                    pluginModuleList.add(image);
                    pluginModuleList.add(apk);
                    pluginModuleList.add(insert);
//                    pluginModuleList.remove(file);
//                    pluginModuleList.remove(locationPlugin);

        } else {
            //pluginModuleList.add(image);
        }

        return pluginModuleList;
    }

    private void removeFilePlugin() {

        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();

        Log.i("TAG", "moduleList.size() =  " + moduleList);

        if (moduleList != null) {

            IExtensionModule module = null;

            for (IExtensionModule extensionModule : moduleList) {

                Log.i("TAG", "extensionModule.getClass().getSimpleName() = " + extensionModule.getClass().getSimpleName());

                if (extensionModule instanceof DefaultExtensionModule) {

                    module = extensionModule;

                    break;

                }

            }

            RongExtensionManager.getInstance().unregisterExtensionModule(module);//注销之前的

            RongExtensionManager.getInstance().registerExtensionModule(new ApkExtensionModule());//注册新的

            List<IExtensionModule> moduleList2 = RongExtensionManager.getInstance().getExtensionModules();

            Log.i("TAG", "moduleList.size() = " + moduleList2);

        }

    }
}
