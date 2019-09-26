package cn.sa.im.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.lang.ref.WeakReference;

import io.rong.imkit.RongIM;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.CircleProgressView;
import io.rong.imkit.widget.provider.GIFMessageItemProvider;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.GIFMessage;

import static io.rong.imkit.widget.provider.GIFMessageItemProvider.dip2px;

@ProviderTag(
        messageContent = GIFMessage.class,
        showProgress = false,
        showReadState = true)
public class GifMessagesaItemProvider extends IContainerItemProvider.MessageProvider<GIFMessage> {

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(io.rong.imkit.R.layout.rc_item_gif_message, (ViewGroup)null);
        GifMessagesaItemProvider.ViewHolder holder = new GifMessagesaItemProvider.ViewHolder();
        holder.img = (AsyncImageView)view.findViewById(io.rong.imkit.R.id.rc_img);
        holder.preProgress = (ProgressBar)view.findViewById(io.rong.imkit.R.id.rc_pre_progress);
        holder.loadingProgress = (CircleProgressView)view.findViewById(io.rong.imkit.R.id.rc_gif_progress);
        holder.startDownLoad = (ImageView)view.findViewById(io.rong.imkit.R.id.rc_start_download);
        holder.downLoadFailed = (ImageView)view.findViewById(io.rong.imkit.R.id.rc_download_failed);
        holder.length = (TextView)view.findViewById(io.rong.imkit.R.id.rc_length);
        holder.fireView = (FrameLayout)view.findViewById(io.rong.imkit.R.id.rc_destruct_click);
        holder.sendFire = (FrameLayout)view.findViewById(io.rong.imkit.R.id.fl_send_fire);
        holder.receiverFire = (FrameLayout)view.findViewById(io.rong.imkit.R.id.fl_receiver_fire);
        holder.receiverFireImg = (ImageView)view.findViewById(io.rong.imkit.R.id.iv_receiver_fire);
        holder.receiverFireText = (TextView)view.findViewById(io.rong.imkit.R.id.tv_receiver_fire);
        holder.clickHint = (TextView)view.findViewById(io.rong.imkit.R.id.rc_destruct_click_hint);
        view.setTag(holder);
        return view;
    }
    public void bindView(View v, int position, GIFMessage content, UIMessage message) {
        GifMessagesaItemProvider.ViewHolder holder = (GifMessagesaItemProvider.ViewHolder)v.getTag();
        holder.startDownLoad.setVisibility(8);
        holder.downLoadFailed.setVisibility(8);
        holder.preProgress.setVisibility(8);
        holder.loadingProgress.setVisibility(8);
        holder.length.setVisibility(8);
        int[] paramsValue = this.getParamsValue(v.getContext(), content.getWidth(), content.getHeight());
        holder.img.setLayoutParam(paramsValue[0], paramsValue[1]);
        holder.img.setImageDrawable(v.getContext().getResources().getDrawable(io.rong.imkit.R.drawable.def_gif_bg));
        int progress = message.getProgress();
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {
            Message.SentStatus status = message.getSentStatus();
            if (progress > 0 && progress < 100) {
                holder.loadingProgress.setProgress(progress, true);
                holder.loadingProgress.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
            } else if (status.equals(Message.SentStatus.SENDING)) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.VISIBLE);
            } else if (progress == -1) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
            } else {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
            }
        } else if (message.getReceivedStatus().isDownload()) {
            if (progress > 0 && progress < 100) {
                holder.loadingProgress.setProgress(progress, true);
                holder.loadingProgress.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else if (progress == 100) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.length.setVisibility(View.GONE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else if (progress == -1) {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.startDownLoad.setVisibility(View.GONE);
            } else {
                holder.loadingProgress.setVisibility(View.GONE);
                holder.preProgress.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.startDownLoad.setVisibility(View.GONE);
            }
        } else {
            holder.loadingProgress.setVisibility(View.GONE);
            holder.preProgress.setVisibility(View.GONE);
            holder.length.setVisibility(View.GONE);
            holder.startDownLoad.setVisibility(View.GONE);
            if (progress == -1) {
                holder.downLoadFailed.setVisibility(View.VISIBLE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            }
        }

        if (content.getLocalPath() != null) {
            if (content.isDestruct()) {
                holder.fireView.setVisibility(View.VISIBLE);
                holder.img.setVisibility(View.GONE);
                Drawable drawable;
                if (message.getMessageDirection() == Message.MessageDirection.SEND) {
                    holder.sendFire.setVisibility(View.VISIBLE);
                    holder.receiverFire.setVisibility(View.GONE);
                    holder.fireView.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_no_right);
                    drawable = v.getContext().getResources().getDrawable(io.rong.imkit.R.drawable.rc_fire_sender_album);
                    drawable.setBounds(0, 0, RongUtils.dip2px(40.0F), RongUtils.dip2px(34.0F));
                    holder.clickHint.setCompoundDrawables((Drawable)null, drawable, (Drawable)null, (Drawable)null);
                    holder.clickHint.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    holder.sendFire.setVisibility(View.GONE);
                    holder.receiverFire.setVisibility(View.VISIBLE);
                    holder.fireView.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_no_left);
                    drawable = v.getContext().getResources().getDrawable(io.rong.imkit.R.drawable.rc_fire_receiver_album);
                    drawable.setBounds(0, 0, RongUtils.dip2px(40.0F), RongUtils.dip2px(34.0F));
                    holder.clickHint.setCompoundDrawables((Drawable)null, drawable, (Drawable)null, (Drawable)null);
                    holder.clickHint.setTextColor(Color.parseColor("#F4B50B"));
                    DestructManager.getInstance().addListener(message.getUId(), new GifMessagesaItemProvider.DestructListener(holder, message.getUId()), "GIFMessageItemProvider");
                    if (message.getMessage().getReadTime() > 0L) {
                        holder.receiverFireText.setVisibility(View.VISIBLE);
                        holder.receiverFireImg.setVisibility(View.GONE);
                    } else {
                        holder.receiverFireText.setVisibility(View.GONE);
                        holder.receiverFireImg.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                holder.fireView.setVisibility(View.GONE);
                holder.img.setVisibility(View.VISIBLE);
                this.loadGif(v, content.getLocalUri(), holder);
            }
        } else {
            int size = v.getContext().getResources().getInteger(io.rong.imkit.R.integer.rc_gifmsg_auto_download_size);
            if (content.getGifDataSize() <= (long)(size * 1024)) {
                if (this.checkPermission(v.getContext())) {
                    if (!message.getReceivedStatus().isDownload()) {
                        message.getReceivedStatus().setDownload();
                        this.downLoad(message.getMessage(), holder);
                    }
                } else if (progress != -1) {
                    holder.startDownLoad.setVisibility(View.VISIBLE);
                    holder.length.setVisibility(View.VISIBLE);
                    holder.length.setText(this.formatSize(content.getGifDataSize()));
                }
            } else if (progress > 0 && progress < 100) {
                holder.startDownLoad.setVisibility(View.GONE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            } else if (progress != -1) {
                holder.startDownLoad.setVisibility(View.VISIBLE);
                holder.preProgress.setVisibility(View.GONE);
                holder.loadingProgress.setVisibility(View.GONE);
                holder.downLoadFailed.setVisibility(View.GONE);
                holder.length.setVisibility(View.VISIBLE);
                holder.length.setText(this.formatSize(content.getGifDataSize()));
            }
        }

    }

    @Override
    public Spannable getContentSummary(GIFMessage gifMessage) {
        return null;
    }

    @Override
    public void onItemClick(View view, int i, GIFMessage gifMessage, UIMessage uiMessage) {

    }

    private int[] getParamsValue(Context context, int width, int height) {
        int maxWidth = dip2px(context, 120.0F);
        int minValue = dip2px(context, 80.0F);

        float scale;
        int finalWidth;
        int finalHeight;
        if (width > maxWidth) {
            finalWidth = maxWidth;
            scale = (float)width / (float)maxWidth;
            finalHeight = Math.round((float)height / scale);
            if (finalHeight < minValue) {
                finalHeight = minValue;
            }
        } else if (width < minValue) {
            finalWidth = minValue;
            scale = (float)width / (float)minValue;
            finalHeight = Math.round((float)height * scale);
            if (finalHeight < minValue) {
                finalHeight = minValue;
            }
        } else {
            finalWidth = Math.round((float)height);
            finalHeight = Math.round((float)width);
        }

        int[] params = new int[]{finalWidth, finalHeight};
        return params;
    }
    private String formatSize(long length) {
        float size;
        if (length > 1048576L) {
            size = (float)Math.round((float)length / 1048576.0F * 100.0F) / 100.0F;
            return size + "M";
        } else if (length > 1024L) {
            size = (float)Math.round((float)length / 1024.0F * 100.0F) / 100.0F;
            return size + "KB";
        } else {
            return length + "B";
        }
    }
    private void loadGif(View v, Uri uri, GifMessagesaItemProvider.ViewHolder holder) {
        ((RequestBuilder) Glide.with(v.getContext()).asGif().diskCacheStrategy(DiskCacheStrategy.RESOURCE)).load(uri.getPath()).listener(new RequestListener<GifDrawable>() {
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(holder.img);
    }

    private static class DestructListener implements RongIMClient.DestructCountDownTimerListener {
        private WeakReference<GifMessagesaItemProvider.ViewHolder> mHolder;
        private String mMessageId;

        public DestructListener(GifMessagesaItemProvider.ViewHolder pHolder, String pMessageId) {
            this.mHolder = new WeakReference(pHolder);
            this.mMessageId = pMessageId;
        }

        public void onTick(long millisUntilFinished, String pMessageId) {
            if (this.mMessageId.equals(pMessageId)) {
                GifMessagesaItemProvider.ViewHolder viewHolder = (GifMessagesaItemProvider.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.receiverFireText.setVisibility(0);
                    viewHolder.receiverFireImg.setVisibility(8);
                    viewHolder.receiverFireText.setText(String.valueOf(Math.max(millisUntilFinished, 1L)));
                }
            }

        }

        public void onStop(String messageId) {
            if (this.mMessageId.equals(messageId)) {
                GifMessagesaItemProvider.ViewHolder viewHolder = (GifMessagesaItemProvider.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.receiverFireText.setVisibility(8);
                    viewHolder.receiverFireImg.setVisibility(0);
                }
            }

        }
    }
    private boolean checkPermission(Context context) {
        String[] permission = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
        return PermissionCheckUtil.checkPermissions(context, permission);
    }
    private void downLoad(Message downloadMsg, GifMessagesaItemProvider.ViewHolder holder) {
        holder.preProgress.setVisibility(0);
        RongIM.getInstance().downloadMediaMessage(downloadMsg, (IRongCallback.IDownloadMediaMessageCallback)null);
    }

    private static class ViewHolder {
        AsyncImageView img;
        ProgressBar preProgress;
        CircleProgressView loadingProgress;
        ImageView startDownLoad;
        ImageView downLoadFailed;
        TextView length;
        FrameLayout fireView;
        FrameLayout sendFire;
        FrameLayout receiverFire;
        ImageView receiverFireImg;
        TextView receiverFireText;
        TextView clickHint;

        private ViewHolder() {
        }
    }
}
