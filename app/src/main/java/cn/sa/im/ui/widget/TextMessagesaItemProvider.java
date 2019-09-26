package cn.sa.im.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.TextMessageItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class TextMessagesaItemProvider extends TextMessageItemProvider {
    public TextMessagesaItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(io.rong.imkit.R.layout.rc_item_text_message, (ViewGroup)null);
        TextMessagesaItemProvider.ViewHolder holder = new TextMessagesaItemProvider.ViewHolder();
        holder.message = (AutoLinkTextView)view.findViewById(16908308);
        holder.unRead = (TextView)view.findViewById(io.rong.imkit.R.id.tv_unread);
        holder.sendFire = (FrameLayout)view.findViewById(io.rong.imkit.R.id.fl_send_fire);
        holder.receiverFire = (FrameLayout)view.findViewById(io.rong.imkit.R.id.fl_receiver_fire);
        holder.receiverFireImg = (ImageView)view.findViewById(io.rong.imkit.R.id.iv_receiver_fire);
        holder.receiverFireText = (TextView)view.findViewById(io.rong.imkit.R.id.tv_receiver_fire);
        view.setTag(holder);
        return view;
    }

    public Spannable getContentSummary(TextMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, TextMessage data) {
        if (data == null) {
            return null;
        } else if (data.isDestruct()) {
            return new SpannableString(context.getString(io.rong.imkit.R.string.rc_message_content_burn));
        } else {
            String content = data.getContent();
            if (content != null) {
                if (content.length() > 100) {
                    content = content.substring(0, 100);
                }

                return new SpannableString(AndroidEmoji.ensure(content));
            } else {
                return null;
            }
        }
    }

    public void onItemClick(View view, int position, TextMessage content, UIMessage message) {
        TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder)view.getTag();
        if (content != null && content.isDestruct() && message.getMessage().getReadTime() <= 0L) {
            holder.unRead.setVisibility(8);
            holder.message.setVisibility(0);
            holder.receiverFireText.setVisibility(0);
            holder.receiverFireImg.setVisibility(8);
            this.processTextView(view, position, content, message, holder.message);
            DestructManager.getInstance().startDestruct(message.getMessage());
        }

    }

    public void bindView(View v, int position, TextMessage content, UIMessage data) {
        TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder)v.getTag();
        if (data.getMessageDirection() == Message.MessageDirection.SEND) {
            holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }

        if (content.isDestruct()) {
            this.bindFireView(v, position, content, data);
        } else {
            holder.sendFire.setVisibility(8);
            holder.receiverFire.setVisibility(8);
            holder.unRead.setVisibility(8);
            holder.message.setVisibility(0);
            AutoLinkTextView textView = holder.message;
            this.processTextView(v, position, content, data, textView);
        }

    }

    private void processTextView(final View v, final int position, final TextMessage content, final UIMessage data, final AutoLinkTextView pTextView) {
        if (data.getTextMessageContent() != null) {
            int len = data.getTextMessageContent().length();
            if (v.getHandler() != null && len > 500) {
                v.getHandler().postDelayed(new Runnable() {
                    public void run() {
                        pTextView.setText(data.getTextMessageContent());
                    }
                }, 50L);
            } else {
                pTextView.setText(data.getTextMessageContent());
            }
        }

        pTextView.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
            public boolean onLinkClick(String link) {
                RongIM.ConversationBehaviorListener listener = RongContext.getInstance().getConversationBehaviorListener();
                RongIM.ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
                boolean result = false;
                if (listener != null) {
                    result = listener.onMessageLinkClick(v.getContext(), link);
                } else if (clickListener != null) {
                    result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());
                }

                if (listener == null && clickListener == null || !result) {
                    String str = link.toLowerCase();
                    if (str.startsWith("http") || str.startsWith("https")) {
                        Intent intent = new Intent("io.rong.imkit.intent.action.webview");
                        intent.setPackage(v.getContext().getPackageName());
                        intent.putExtra("url", link);
                        v.getContext().startActivity(intent);
                        result = true;
                    }
                }

                return result;
            }
        }));
        pTextView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                TextMessagesaItemProvider.this.onItemLongClick(v, position, content, data);
                return true;
            }
        });
        pTextView.stripUnderlines();
    }

    private void bindFireView(View pV, int pPosition, TextMessage pContent, UIMessage pData) {
        TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder)pV.getTag();
        if (pData.getMessageDirection() == Message.MessageDirection.SEND) {
            holder.sendFire.setVisibility(0);
            holder.receiverFire.setVisibility(8);
            holder.unRead.setVisibility(8);
            holder.message.setVisibility(0);
            this.processTextView(pV, pPosition, pContent, pData, holder.message);
        } else {
            holder.sendFire.setVisibility(8);
            holder.receiverFire.setVisibility(0);
            DestructManager.getInstance().addListener(pData.getUId(), new TextMessagesaItemProvider.DestructListener(holder, pData.getUId()), "TextMessageItemProvider");
            if (pData.getMessage().getReadTime() > 0L) {
                holder.unRead.setVisibility(8);
                holder.message.setVisibility(0);
                holder.receiverFireText.setVisibility(0);
                holder.receiverFireText.setText("");
                holder.receiverFireImg.setVisibility(8);
                this.processTextView(pV, pPosition, pContent, pData, holder.message);
            } else {
                holder.unRead.setVisibility(0);
                holder.message.setVisibility(8);
                holder.receiverFireText.setVisibility(8);
                holder.receiverFireImg.setVisibility(0);
            }
        }

    }

    private static class DestructListener implements RongIMClient.DestructCountDownTimerListener {
        private WeakReference<TextMessagesaItemProvider.ViewHolder> mHolder;
        private String mMessageId;

        public DestructListener(TextMessagesaItemProvider.ViewHolder pHolder, String pMessageId) {
            this.mHolder = new WeakReference(pHolder);
            this.mMessageId = pMessageId;
        }

        public void onTick(long millisUntilFinished, String pMessageId) {
            if (this.mMessageId.equals(pMessageId)) {
                TextMessagesaItemProvider.ViewHolder viewHolder = (TextMessagesaItemProvider.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.receiverFireText.setText(String.valueOf(Math.max(millisUntilFinished, 1L)));
                }
            }

        }

        public void onStop(String messageId) {
            if (this.mMessageId.equals(messageId)) {
                TextMessagesaItemProvider.ViewHolder viewHolder = (TextMessagesaItemProvider.ViewHolder)this.mHolder.get();
                if (viewHolder != null) {
                    viewHolder.receiverFireText.setVisibility(8);
                    viewHolder.receiverFireImg.setVisibility(0);
                }
            }

        }
    }

    private static class ViewHolder {
        AutoLinkTextView message;
        TextView unRead;
        FrameLayout sendFire;
        FrameLayout receiverFire;
        ImageView receiverFireImg;
        TextView receiverFireText;

        private ViewHolder() {
        }
    }


}
