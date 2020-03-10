package cn.sa.im.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import cn.sa.im.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongKitIntent;
import io.rong.imkit.destruct.DestructManager;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imkit.widget.provider.TextMessageItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;
@ProviderTag(
        messageContent = TextMessage.class,
        showReadState = true
)
public class TextMessagesaItemProvider extends TextMessageItemProvider {
        private static final String TAG = "TextMessagesaItemProvider";

        private static class ViewHolder {
            AutoLinkTextView message;
            TextView unRead;
            FrameLayout sendFire;
            FrameLayout receiverFire;
            ImageView receiverFireImg;
            TextView receiverFireText;
        }

        @Override
        public View newView(Context context, ViewGroup group) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_destruct_text_message, null);
            TextMessagesaItemProvider.ViewHolder holder = new TextMessagesaItemProvider.ViewHolder();
            holder.message = view.findViewById(android.R.id.text1);
            holder.unRead = view.findViewById(R.id.tv_unread);
            holder.sendFire = view.findViewById(R.id.fl_send_fire);
            holder.receiverFire = view.findViewById(R.id.fl_receiver_fire);
            holder.receiverFireImg = view.findViewById(R.id.iv_receiver_fire);
            holder.receiverFireText = view.findViewById(R.id.tv_receiver_fire);
            view.setTag(holder);
            return view;
        }

        @Override
        public Spannable getContentSummary(TextMessage data) {
            return null;
        }

        @Override
        public Spannable getContentSummary(Context context, TextMessage data) {
            if (data == null)
                return null;
            if (data.isDestruct()) {
                return new SpannableString(context.getString(io.rong.imkit.R.string.rc_message_content_burn));
            }
            String content = data.getContent();
            if (content != null) {
                if (content.length() > 100) {
                    content = content.substring(0, 100);
                }
                return new SpannableString(AndroidEmoji.ensure(content));
            }
            return null;
        }

        @Override
        public void onItemClick(View view, int position, TextMessage content, UIMessage message) {
            TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder) view.getTag();
            if (content != null && content.isDestruct() && !(message.getMessage().getReadTime() > 0)) {
                holder.unRead.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                holder.receiverFireText.setVisibility(View.VISIBLE);
                holder.receiverFireImg.setVisibility(View.GONE);
                processTextView(view, position, content, message, holder.message);
                DestructManager.getInstance().startDestruct(message.getMessage());
            }
        }

        @Override
        public void bindView(final View v, int position, TextMessage content, final UIMessage data) {
                    if (content.getContent() != null) {
                        if(content.getContent().equals("[(D)]")) {
                            int code = AndroidEmoji.getEmojiCode(0);
                            char[] chars = Character.toChars(code);
                            String key = Character.toString(chars[0]);
                            for (int i = 1; i < chars.length; ++i) {
                                key = key + Character.toString(chars[i]);
                            }
                            data.setTextMessageContent(new SpannableStringBuilder(AndroidEmoji.ensure(key)));
                        }
                    }

            TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder) v.getTag();
            if (data.getMessageDirection() == Message.MessageDirection.SEND) {
                holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
            } else {
                holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
            }
            if (content.isDestruct()) {
                bindFireView(v, position, content, data);
            } else {
                holder.sendFire.setVisibility(View.GONE);
                holder.receiverFire.setVisibility(View.GONE);
                holder.unRead.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                final AutoLinkTextView textView = holder.message;
                processTextView(v, position, content, data, textView);
            }
        }

        private void processTextView(final View v, final int position, final TextMessage content, final UIMessage data, final AutoLinkTextView pTextView) {
            if (data.getTextMessageContent() != null) {
                int len = data.getTextMessageContent().length();
                //文本消息过大，缓解下拉滑动时的卡顿问题
                if (v.getHandler() != null && len > 500) {
                    v.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pTextView.setText(data.getTextMessageContent());
                        }
                    }, 50);
                } else {
                    pTextView.setText(data.getTextMessageContent());

                }
            }

            pTextView.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
                @Override
                public boolean onLinkClick(String link) {
                    RongIM.ConversationBehaviorListener listener = RongContext.getInstance().getConversationBehaviorListener();
                    RongIM.ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
                    boolean result = false;
                    if (listener != null) {
                        result = listener.onMessageLinkClick(v.getContext(), link);
                    } else if (clickListener != null) {
                        result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());
                    }
                    if ((listener == null && clickListener == null) || !result) {
                        String str = link.toLowerCase();
                        if (str.startsWith("http") || str.startsWith("https")) {
                            Intent intent = new Intent(RongKitIntent.RONG_INTENT_ACTION_WEBVIEW);
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
                @Override
                public boolean onLongClick(View v) {
                    TextMessagesaItemProvider.this.onItemLongClick(v, position, content, data);
                    return true;
                }
            });
            pTextView.stripUnderlines();
        }

        private void bindFireView(View pV, int pPosition, TextMessage pContent, final UIMessage pData) {
            TextMessagesaItemProvider.ViewHolder holder = (TextMessagesaItemProvider.ViewHolder) pV.getTag();
            if (pData.getMessageDirection() == Message.MessageDirection.SEND) {
                holder.sendFire.setVisibility(View.VISIBLE);
                holder.receiverFire.setVisibility(View.GONE);
                holder.unRead.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                processTextView(pV, pPosition, pContent, pData, holder.message);
            } else {
                holder.sendFire.setVisibility(View.GONE);
                holder.receiverFire.setVisibility(View.VISIBLE);
                DestructManager.getInstance().addListener(pData.getUId(), new TextMessagesaItemProvider.DestructListener(holder, pData), TAG);
                //getReadTime>0，证明已读，开始倒计时
                if (pData.getMessage().getReadTime() > 0) {
                    holder.unRead.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    holder.receiverFireText.setVisibility(View.VISIBLE);
                    String unFinishTime;
                    if (TextUtils.isEmpty(pData.getUnDestructTime())) {
                        unFinishTime = DestructManager.getInstance().getUnFinishTime(pData.getUId());
                    } else {
                        unFinishTime = pData.getUnDestructTime();
                    }
                    holder.receiverFireText.setText(unFinishTime);
                    holder.receiverFireImg.setVisibility(View.GONE);
                    processTextView(pV, pPosition, pContent, pData, holder.message);
                    DestructManager.getInstance().startDestruct(pData.getMessage());
                } else {
                    holder.unRead.setVisibility(View.VISIBLE);
                    holder.message.setVisibility(View.GONE);
                    holder.receiverFireText.setVisibility(View.GONE);
                    holder.receiverFireImg.setVisibility(View.VISIBLE);
                }
            }
        }


        private static class DestructListener implements RongIMClient.DestructCountDownTimerListener {
            private WeakReference<TextMessagesaItemProvider.ViewHolder> mHolder;
            private UIMessage mUIMessage;

            public DestructListener(TextMessagesaItemProvider.ViewHolder pHolder, UIMessage pUIMessage) {
                mHolder = new WeakReference<>(pHolder);
                mUIMessage = pUIMessage;
            }

            @Override
            public void onTick(long millisUntilFinished, String pMessageId) {
                if (mUIMessage.getUId().equals(pMessageId)) {
                    TextMessagesaItemProvider.ViewHolder viewHolder = mHolder.get();
                    if (viewHolder != null) {
                        viewHolder.receiverFireText.setVisibility(View.VISIBLE);
                        viewHolder.receiverFireImg.setVisibility(View.GONE);
                        String unDestructTime = String.valueOf(Math.max(millisUntilFinished, 1));
                        viewHolder.receiverFireText.setText(unDestructTime);
                        mUIMessage.setUnDestructTime(unDestructTime);
                    }
                }
            }

            @Override
            public void onStop(String messageId) {
                if (mUIMessage.getUId().equals(messageId)) {
                    TextMessagesaItemProvider.ViewHolder viewHolder = mHolder.get();
                    if (viewHolder != null) {
                        viewHolder.receiverFireText.setVisibility(View.GONE);
                        viewHolder.receiverFireImg.setVisibility(View.VISIBLE);
                        mUIMessage.setUnDestructTime(null);
                    }
                }
            }

            public void setHolder(TextMessagesaItemProvider.ViewHolder pHolder) {
                mHolder = new WeakReference<>(pHolder);
            }

            public void setUIMessage(UIMessage pUIMessage) {
                mUIMessage = pUIMessage;
            }
        }
    }
