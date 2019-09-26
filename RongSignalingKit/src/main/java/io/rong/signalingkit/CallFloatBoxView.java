package io.rong.signalingkit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.rong.signalingkit.callmanager.RCSCallListenerProxy;
import io.rong.signalingkit.callmessage.CallSTerminateMessage;
import io.rong.signalingkit.util.BluetoothUtil;
import io.rong.signalingkit.util.CallKitUtils;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.utils.NotificationUtil;
import io.rong.imlib.model.Conversation;
import io.rong.signalingkit.callmanager.RCSCallUserProfile;
import io.rong.signalingkit.callmanager.IRCSCallListener;
import io.rong.message.InformationNotificationMessage;

import static io.rong.signalingkit.util.CallKitUtils.isDial;

/**
 * Created by weiqinxiao on 16/3/17.
 */
public class CallFloatBoxView {
    private static final String TAG = CallFloatBoxView.class.getSimpleName();
    private static Context mContext;
    private static Timer timer;
    private static long mTime;
    private static View mView;
    private static Boolean isShown = false;
    private static WindowManager wm;
    private static Bundle mBundle;
    private static TextView showFBCallTime = null;
    private static FrameLayout remoteVideoContainer = null;

    public static boolean getIsShown(){
        return isShown;
    }

    public static void showFB(Context context, Bundle bundle) {
        Log.i("audioTag", "CallKitUtils.isDial=" + CallKitUtils.isDial);
        if (CallKitUtils.isDial) {
            CallFloatBoxView.showFloatBoxToCall(context, bundle);
        } else {
            CallFloatBoxView.showFloatBox(context, bundle);
        }
    }

    public static void showFloatBox(Context context, Bundle bundle) {
        if (isShown) {
            return;
        }
        mContext = context;
        isShown = true;
        RCSCallSession session = RCSCallClient.getInstance().getCallSession();
        long activeTime = session != null ? session.getActiveTime() : 0;
        mTime = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;
        if (mTime > 0) {
            setAudioMode(AudioManager.MODE_IN_COMMUNICATION);
        }

        mBundle =(Bundle) bundle.clone();
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = getLayoutParams(context);

        View.OnTouchListener onTouchListener = getOnTouchListener(params);

        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
        mView.setOnTouchListener(onTouchListener);
        wm.addView(mView, params);
        TextView timeV = (TextView) mView.findViewById(R.id.rc_time);
        setupTime(timeV);
        ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
        RCSCallCommon.CallMediaType mediaType = RCSCallCommon.CallMediaType.valueOf(bundle.getInt("mediaType"));
        if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
        } else {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
        }

        if (mediaType == RCSCallCommon.CallMediaType.VIDEO && session != null
                && session.getConversationType() == Conversation.ConversationType.PRIVATE) {
            List<RCSCallUserProfile> remoteUsers = RCSCallClient.getInstance().getCallSession().getRemoteUsers();
            SurfaceView remoteVideo = remoteUsers.size() > 0 ? remoteUsers.get(0).getVideoView() : null;
            if (remoteVideo != null) {
                ViewGroup parent = (ViewGroup) remoteVideo.getParent();
                if (parent != null) {
                    parent.removeView(remoteVideo);
                }
                Resources resources = mContext.getResources();
                params.width = resources.getDimensionPixelSize(R.dimen.callkit_dimen_size_60);
                params.height = resources.getDimensionPixelSize(R.dimen.callkit_dimen_size_80);
                remoteVideoContainer = new FrameLayout(mContext);
                remoteVideoContainer.addView(remoteVideo, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                remoteVideoContainer.setOnTouchListener(onTouchListener);
                wm.addView(remoteVideoContainer, params);
            }
        }
        RCSCallListenerProxy.getInstance().registerCallListener(callListener);
    }

    private static View.OnTouchListener getOnTouchListener(final WindowManager.LayoutParams params) {
        return new View.OnTouchListener() {
                float lastX, lastY;
                int oldOffsetX, oldOffsetY;
                int tag = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    RLog.e(TAG, "onTouch: ");
                    final int action = event.getAction();
                    float x = event.getX();
                    float y = event.getY();
                    if (tag == 0) {
                        oldOffsetX = params.x;
                        oldOffsetY = params.y;
                    }
                    if (action == MotionEvent.ACTION_DOWN) {
                        lastX = x;
                        lastY = y;
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        // 减小偏移量,防止过度抖动
                        params.x += (int) (x - lastX) / 3;
                        params.y += (int) (y - lastY) / 3;
                        tag = 1;
                        if (mView != null) {
                            wm.updateViewLayout(mView, params);
                        }
                        if (remoteVideoContainer != null) {
                            wm.updateViewLayout(remoteVideoContainer, params);
                        }
                    } else if (action == MotionEvent.ACTION_UP) {
                        RLog.e(TAG, "onTouch: ACTION_UP");
                        int newOffsetX = params.x;
                        int newOffsetY = params.y;
                        if (Math.abs(oldOffsetX - newOffsetX) <= 20 && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                            onClickToResume();
                        } else {
                            tag = 0;
                        }
                    }
                    return true;
                }
            };
    }

    public static void showFloatBoxToCall(Context context, Bundle bundle) {
        if (isShown) {
            return;
        }
        mContext = context;
        isShown = true;

        mBundle = bundle;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = getLayoutParams(context);

        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
        mView.setOnTouchListener(getOnTouchListener(params));
        wm.addView(mView, params);
        showFBCallTime = (TextView) mView.findViewById(R.id.rc_time);
        showFBCallTime.setVisibility(View.GONE);

        ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
        RCSCallCommon.CallMediaType mediaType = RCSCallCommon.CallMediaType.valueOf(bundle.getInt("mediaType"));
        if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
        } else {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
        }
        RCSCallListenerProxy.getInstance().registerCallListener(callListener);
    }

    private static WindowManager.LayoutParams getLayoutParams(Context context) {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < 24) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.x = context.getResources().getDisplayMetrics().widthPixels;
        params.y = 0;
        return params;
    }

    /***
     * 调用showFloatBoxToCall 之后 调用该方法设置
     */
    public static void showFloatBoxToCallTime() {
        if (!isShown) {
            return;
        }
        RCSCallSession session = RCSCallClient.getInstance().getCallSession();
        long activeTime = session != null ? session.getActiveTime() : 0;
        mTime = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;
        mView = LayoutInflater.from(mContext).inflate(R.layout.rc_voip_float_box, null);
        TextView timeV = (TextView) mView.findViewById(R.id.rc_time);
        if (null != showFBCallTime) {
            setupTime(showFBCallTime);
        }
    }

    public static void hideFloatBox() {
        RLog.i(TAG, "hideFloatBox");
        RCSCallListenerProxy.getInstance().unregisterCallListener(callListener);
        if (isShown) {
            if (mView != null) {
                wm.removeView(mView);
            }
            detachRemoteVideoView();
            if (null != timer) {
                timer.cancel();
                timer = null;
            }
            isShown = false;
            mView = null;
            mTime = 0;
            mBundle = null;
            showFBCallTime = null;
            CallKitUtils.shouldShowFloat = false;
        }
    }

    private static void detachRemoteVideoView() {
        if (remoteVideoContainer != null && RCSCallClient.getInstance().getCallSession() != null) {
            List<RCSCallUserProfile> remoteUsers = RCSCallClient.getInstance().getCallSession().getRemoteUsers();
            SurfaceView remoteVideo = remoteUsers.size() > 0 ? remoteUsers.get(0).getVideoView() : null;
            if (RCSCallClient.getInstance().getMyCallUserProfile() != null) {
                SurfaceView myVideoView = RCSCallClient.getInstance().getMyCallUserProfile().getVideoView();
                if (myVideoView != null) {
                    remoteVideoContainer.removeView(remoteVideo);
                }
            }
            wm.removeView(remoteVideoContainer);
            remoteVideoContainer = null;
        }
    }

    public static Intent getResumeIntent() {
        if (mBundle == null) {
            return null;
        }
        mBundle.putBoolean("isDial", isDial);
        RCSCallListenerProxy.getInstance().unregisterCallListener(callListener);
        Intent intent = new Intent(mBundle.getString("action"));
        intent.putExtra("floatbox", mBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callAction", RCSCallAction.ACTION_RESUME_CALL.getName());

        return new Intent();
    }

    public static void onClickToResume() {
        RLog.e(TAG, "onClickToResume: ");
        //当快速双击悬浮窗时，第一次点击之后会把mBundle置为空，第二次点击的时候出现NPE
        if (mBundle == null) {
            RLog.d(TAG, "onClickToResume mBundle is null");
            return;
        }

        mBundle.putBoolean("isDial", isDial);
        RCSCallListenerProxy.getInstance().unregisterCallListener(callListener);

        Intent intent = new Intent(mBundle.getString("action"));
        intent.setPackage(mContext.getPackageName());
        intent.putExtra("floatbox", mBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callAction", RCSCallAction.ACTION_RESUME_CALL.getName());

        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            pendingIntent.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupTime(final TextView timeView) {
        final Handler handler = new Handler(Looper.getMainLooper());
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTime++;
                        if (mTime >= 3600) {
                            timeView.setText(String.format("%d:%02d:%02d", mTime / 3600, (mTime % 3600) / 60, (mTime % 60)));
                            timeView.setVisibility(View.VISIBLE);
                        } else {
                            timeView.setText(String.format("%02d:%02d", (mTime % 3600) / 60, (mTime % 60)));
                            timeView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        };

        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    private static void setAudioMode(int mode) {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMode(mode);
        }
    }

    private static IRCSCallListener callListener = new IRCSCallListener() {
        @Override
        public void onCallOutgoing(RCSCallSession callInfo, SurfaceView localVideo) {

        }

        @Override
        public void onRemoteUserRinging(String userId) {

        }

        @Override
        public void onCallDisconnected(RCSCallSession callProfile, RCSCallCommon.CallDisconnectedReason reason) {
            RLog.i(TAG, "onCallDisconnected " + reason);
            String senderId;
            String extra = "";
            senderId = callProfile.getInviterUserId();
            switch (reason) {
                case HANGUP:
                case REMOTE_HANGUP:
                        if (mTime >= 3600) {
                            extra = String.format("%d:%02d:%02d", mTime / 3600, (mTime % 3600) / 60, (mTime % 60));
                        } else {
                            extra = String.format("%02d:%02d", (mTime % 3600) / 60, (mTime % 60));
                        }
                    break;
            }
            if (!TextUtils.isEmpty(senderId)) {
                switch (callProfile.getConversationType()) {
                    case PRIVATE:
                            CallSTerminateMessage callSTerminateMessage = new CallSTerminateMessage();
                            callSTerminateMessage.setReason(reason);
                            callSTerminateMessage.setMediaType(callProfile.getMediaType());
                            callSTerminateMessage.setExtra(extra);
                            if (senderId.equals(callProfile.getSelfUserId())) {
                                callSTerminateMessage.setDirection("MO");
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, callSTerminateMessage, null);
                            } else {
                                callSTerminateMessage.setDirection("MT");
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        senderId, receivedStatus, callSTerminateMessage, null);
                            }
                        break;
                    case GROUP:
                            InformationNotificationMessage informationNotificationMessage;
                            if (reason.equals(RCSCallCommon.CallDisconnectedReason.NO_RESPONSE)) {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_no_response));
                            } else {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_ended));
                            }

                            if (senderId.equals(callProfile.getSelfUserId())) {
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, informationNotificationMessage, null);
                            } else {
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        senderId, receivedStatus, informationNotificationMessage, null);
                            }
                        break;
                    default:
                        break;
                }
            }
            Toast.makeText(mContext, mContext.getString(R.string.rc_voip_call_terminalted), Toast.LENGTH_SHORT).show();
            RLog.e(TAG, "onCallDisconnected: ============================");
            if (wm != null && mView != null) {
                wm.removeView(mView);
                if (null != timer) {
                    timer.cancel();
                    timer = null;
                }
                isShown = false;
                mView = null;
                mTime = 0;
            }
            detachRemoteVideoView();
            setAudioMode(AudioManager.MODE_NORMAL);
            AudioPlayManager.getInstance().setInVoipMode(false);
            NotificationUtil.clearNotification(mContext, BaseCallActivity.CALL_NOTIFICATION_ID);
            RCSCallListenerProxy.getInstance().unregisterCallListener(this);
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            BluetoothUtil.stopBlueToothSco(mContext);
            hideFloatBox();
        }

        @Override
        public void onRemoteUserLeft(String userId, RCSCallCommon.CallDisconnectedReason reason) {

        }

        @Override
        public void onMediaTypeChanged(String userId, RCSCallCommon.CallMediaType mediaType, SurfaceView video) {
            if (RCSCallClient.getInstance().getCallSession().getMediaType() != RCSCallCommon.CallMediaType.AUDIO) {
                RCSCallClient.getInstance().changeMediaType(RCSCallCommon.CallMediaType.AUDIO);
                RCSCallClient.getInstance().getCallSession().setMediaType(RCSCallCommon.CallMediaType.AUDIO);
            }

            if (mView != null) {
                wm.removeView(mView);
            }
            detachRemoteVideoView();
            if (mView == null) {
                mView = LayoutInflater.from(mContext).inflate(R.layout.rc_voip_float_box, null);
            }
            ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
            if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
                final WindowManager.LayoutParams params = getLayoutParams(mContext);
                wm.addView(mView, params);
                showFBCallTime = (TextView) mView.findViewById(R.id.rc_time);
                showFBCallTime.setVisibility(View.GONE);
                mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
                if (null != showFBCallTime) {
                    setupTime(showFBCallTime);
                }
                mView.setOnTouchListener(getOnTouchListener(params));
            } else {
                mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
            }
        }

        @Override
        public void onError(RCSCallCommon.CallErrorCode errorCode) {
            setAudioMode(AudioManager.MODE_NORMAL);
            AudioPlayManager.getInstance().setInVoipMode(false);
        }

        @Override
        public void onCallConnected(RCSCallSession callInfo, SurfaceView localVideo) {
            if (CallKitUtils.isDial && isShown) {
                CallFloatBoxView.showFloatBoxToCallTime();
                CallKitUtils.isDial = false;
            }
            AudioPlayManager.getInstance().setInVoipMode(true);
            setAudioMode(AudioManager.MODE_IN_COMMUNICATION);
        }


        @Override
        public void onRemoteUserJoined(String userId, RCSCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
            if (CallKitUtils.isDial && isShown) {
                CallFloatBoxView.showFloatBoxToCallTime();
                CallKitUtils.isDial = false;
            }
        }

        @Override
        public void onRemoteUserInvited(String userId, RCSCallCommon.CallMediaType mediaType) {

        }

        @Override
        public void onRemoteCameraDisabled(String userId, boolean muted) {

        }

        @Override
        public void onNetworkReceiveLost(String userId, int lossRate) {

        }

        @Override
        public void onNetworkSendLost(int lossRate, int delay) {

        }

        @Override
        public void onFirstRemoteVideoFrame(String userId, int height, int width) {

        }
    };
}
