package cn.sa.im.ui.widget.plugin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.sa.im.R;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Integer> list;
    private OnItemClickListener onItemClickListener;

    public NewsAdapter(List<Integer> list) {
        this.list = list;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_emoji, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        //Log.i("TAG",list.get(position));
        final Context context =holder.itemView.getContext();
        Glide.with(context).load(context.getResources().getDrawable(list.get(position))).into(((MyViewHolder) holder).iv);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StickerSendMessageTask.sendMessage(context,list.get(position));

            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }


}


class MyViewHolder extends RecyclerView.ViewHolder {
    public SquareImageView iv;


    public MyViewHolder(View itemView) {
        super(itemView);
        iv = itemView.findViewById(R.id.emoji_item_iv);
    }
}
