package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.autonavi.amap.mapcore.FileUtil;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cn.sa.im.R;
import cn.sa.im.ui.Utils;
import cn.sa.im.util.RongGenerate;
import io.rong.common.FileUtils;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.utilities.KitStorageUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

public class RongEmoticonTab implements IEmoticonTab {


    public RongEmoticonTab() {

    }

    NewsAdapter adapter;

    @Override
    public Drawable obtainTabDrawable(final Context context) {
        //final String saveImagePath = KitStorageUtils.getImageSavePath(context);
        //return new BitmapDrawable(context.getResources(), file.getPath());
        return context.getResources().getDrawable(R.drawable.u1f603);
    }

    @Override
    public View obtainTabPager(Context context) {
        return initView(context);
    }

    public NewsAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onTableSelected(int i) {
    }

    public View initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_emoji, null);
        RecyclerView rv = view.findViewById(R.id.recycler_view);
        //LinearLayoutManager是用来做列表布局，也就是单列的列表
        GridLayoutManager mLayoutManager = new GridLayoutManager(context, 5, OrientationHelper.VERTICAL, false);
        rv.setLayoutManager(mLayoutManager);

        //谷歌提供了一个默认的item删除添加的动画
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        //模拟列表数据
        ArrayList newsList = new ArrayList<>();
        TypedArray array = context.getResources().obtainTypedArray(context.getResources().getIdentifier("rc_emoji_res", "array", context.getPackageName()));
        int i = -1;
        while (++i < array.length()) {
            newsList.add(array.getResourceId(i, -1));
        }
        adapter = new NewsAdapter(newsList);
        rv.setAdapter(adapter);
        return view;
    }
}


