package cn.sa.im.ui.widget.plugin;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(View view, int position, int res);
    void onItemLongClick(View view, int position, int res);
}
