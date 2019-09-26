package io.rong.signalingkit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import io.rong.signalingkit.callmanager.RCSCallManager;
import io.rong.signalingkit.callmessage.CallSTerminateMessage;
import io.rong.signalingkit.util.BluetoothUtil;
import io.rong.signalingkit.util.CallKitUtils;
import io.rong.signalingkit.util.GlideUtils;
import io.rong.signalingkit.util.HeadsetInfo;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.signalingkit.callmanager.RCSCallUserProfile;
import io.rong.signalingkit.callmanager.RCSSignalCallback;

public class SingleCallActivity extends BaseCallActivity implements Handler.Callback {
    private static final String TAG = "VoIPSingleActivity";
    private LayoutInflater inflater;
    private RCSCallSession callSession;
    private FrameLayout mLPreviewContainer;
    private FrameLayout mSPreviewContainer;
    private FrameLayout mButtonContainer;
    private LinearLayout mUserInfoContainer;
    private TextView mConnectionStateTextView;
    private Boolean isInformationShow = false;
    private SurfaceView mLocalVideo = null;
    private boolean muted = false;
    private boolean handFree = false;
    private boolean startForCheckPermissions = false;
    private boolean isReceiveLost = false;
    private boolean isSendLost = false;
    private SoundPool mSoundPool = null;
    private int EVENT_FULL_SCREEN = 1;

    private String targetId = null;
    private RCSCallCommon.CallMediaType mediaType;

    @Override
    final public boolean handleMessage(Message msg) {
        if (msg.what == EVENT_FULL_SCREEN) {
            hideVideoCallInformation();
            return true;
        }
        return false;
    }

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_voip_activity_single_call);
        RLog.i("AudioPlugin", "savedInstanceState != null=" + (savedInstanceState != null) + ",,,RCSCallClient.getInstance() == null" + (RCSCallClient.getInstance() == null));
        if (savedInstanceState != null && RCSCallClient.getInstance() == null) {
            // 音视频请求权限时，用户在设置页面取消权限，导致应用重启，退出当前activity.
            RLog.i("AudioPlugin", "音视频请求权限时，用户在设置页面取消权限，导致应用重启，退出当前activity");
            finish();
            return;
        }
        Intent intent = getIntent();
        mLPreviewContainer = (FrameLayout) findViewById(R.id.rc_voip_call_large_preview);
        mSPreviewContainer = (FrameLayout) findViewById(R.id.rc_voip_call_small_preview);
        mButtonContainer = (FrameLayout) findViewById(R.id.rc_voip_btn);
        mUserInfoContainer = (LinearLayout) findViewById(R.id.rc_voip_user_info);
        mConnectionStateTextView = findViewById(R.id.rc_tv_connection_state);
        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        RCSCallAction callAction = RCSCallAction.valueOf(intent.getStringExtra("callAction"));

        if (callAction.equals(RCSCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RCSCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RCSCallCommon.CallMediaType.VIDEO;
            }
        } else if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
            intent.setExtrasClassLoader(RCSCallSession.class.getClassLoader());
            callSession = intent.getParcelableExtra("callSession");
            if (callSession == null) {
                callSession = RCSCallClient.getInstance().getCallSession();
            }
            if (callSession != null) {
                mediaType = callSession.getMediaType();
            }
        } else {
            callSession = intent.getParcelableExtra("callSession");
            if (callSession == null) {
                callSession = RCSCallClient.getInstance().getCallSession();
            }
            if (callSession != null) {
                mediaType = callSession.getMediaType();
            }
        }

        if (mediaType != null) {
            inflater = LayoutInflater.from(this);
            initView(mediaType, callAction);
            if (requestCallPermissions(mediaType, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
                setupIntent();
            }
        } else {
            RLog.w(TAG, "恢复的瞬间，对方已挂断");
            setShouldShowFloat(false);
            CallFloatBoxView.hideFloatBox();
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        RCSCallAction callAction = RCSCallAction.valueOf(intent.getStringExtra("callAction"));
        if (callAction == null) {
            return;
        }
        if (callAction.equals(RCSCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RCSCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RCSCallCommon.CallMediaType.VIDEO;
            }
        } else if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
            mediaType = RCSCallClient.getInstance().getCallSession().getMediaType();
        } else {
            mediaType = RCSCallClient.getInstance().getCallSession().getMediaType();
        }
        super.onNewIntent(intent);

        if (requestCallPermissions(mediaType, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
            setupIntent();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                boolean permissionGranted;
                if (mediaType == RCSCallCommon.CallMediaType.AUDIO) {
                    permissionGranted = PermissionCheckUtil.checkPermissions(this, AUDIO_CALL_PERMISSIONS);
                } else {
                    permissionGranted = PermissionCheckUtil.checkPermissions(this, VIDEO_CALL_PERMISSIONS);

                }
                if (permissionGranted) {
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
//                        RCSCallClient.getInstance().onPermissionGranted();
                    } else {
                        setupIntent();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
//                        RCSCallClient.getInstance().onPermissionDenied();
                    } else {
                        Log.i("AudioPlugin", "--onRequestPermissionsResult--finish");
                        finish();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {

            String[] permissions;
            if (mediaType == RCSCallCommon.CallMediaType.AUDIO) {
                permissions = AUDIO_CALL_PERMISSIONS;
            } else {
                permissions = VIDEO_CALL_PERMISSIONS;
            }
            if (PermissionCheckUtil.checkPermissions(this, permissions)) {
                if (startForCheckPermissions) {
//                    RCSCallClient.getInstance().onPermissionGranted();
                } else {
                    setupIntent();
                }
            } else {
                if (startForCheckPermissions) {
//                    RCSCallClient.getInstance().onPermissionDenied();
                } else {
                    Log.i("AudioPlugin", "onActivityResult finish");
                    finish();
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupIntent() {
        RCSCallCommon.CallMediaType mediaType = RCSCallCommon.CallMediaType.AUDIO;
        Intent intent = getIntent();
        RCSCallAction callAction = RCSCallAction.valueOf(intent.getStringExtra("callAction"));
        if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
            mediaType = callSession.getMediaType();
            targetId = callSession.getInviterUserId();
        } else if (callAction.equals(RCSCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RCSCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RCSCallCommon.CallMediaType.VIDEO;
            }
            Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(intent.getStringExtra("conversationType").toUpperCase(Locale.US));
            targetId = intent.getStringExtra("targetId");

            List<String> userIds = new ArrayList<>();
            userIds.add(targetId);
            if (conversationType == Conversation.ConversationType.PRIVATE) {
                RCSCallManager.getInstance().startCall(conversationType, targetId, userIds, mediaType);
            }
        }

        if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
            handFree = false;
        } else if (mediaType.equals(RCSCallCommon.CallMediaType.VIDEO)) {
            handFree = true;
        }

        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
            if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO) || callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null && userInfo.getPortraitUri() != null) {
                    userPortrait.setResource(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
                TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                userName.setText(userInfo.getName());
            }
        }
        if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL) && userInfo != null) {
            ImageView iv_icoming_backgroud = (ImageView) mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);
            if (iv_icoming_backgroud != null) {
                iv_icoming_backgroud.setVisibility(View.VISIBLE);
                GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_icoming_backgroud, null != userInfo ? userInfo.getPortraitUri() : null);
            }
        }
        createPickupDetector();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pickupDetector != null && mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
            pickupDetector.register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pickupDetector != null) {
            pickupDetector.unRegister();
        }
    }

    private void initView(RCSCallCommon.CallMediaType mediaType, RCSCallAction callAction) {
        RelativeLayout buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        RelativeLayout userInfoLayout = null;
        if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO) || callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
            userInfoLayout.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);
        } else {
            //单人视频 or 拨打 界面
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info, null);
            TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
            CallKitUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
        }

        if (callAction.equals(RCSCallAction.ACTION_RESUME_CALL) && CallKitUtils.isDial) {
            try {
                ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
                button.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (callAction.equals(RCSCallAction.ACTION_OUTGOING_CALL)) {
            RelativeLayout layout = buttonLayout.findViewById(R.id.rc_voip_call_mute);
            layout.setVisibility(View.VISIBLE);
            ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(false);
            buttonLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.VISIBLE);
        }

        if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
            mLPreviewContainer.setVisibility(View.GONE);
            mSPreviewContainer.setVisibility(View.GONE);

            if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn = (ImageView) buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallKitUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_audio_call_inviting);
                onIncomingCallRinging();
            }
        } else if (mediaType.equals(RCSCallCommon.CallMediaType.VIDEO)) {
            if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
                findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn = (ImageView) buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_vedio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallKitUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_video_call_inviting);
                onIncomingCallRinging();
            }
        }
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(buttonLayout);
        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoLayout);

        if (callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
            regisHeadsetPlugReceiver();
            if (BluetoothUtil.hasBluetoothA2dpConnected() || BluetoothUtil.isWiredHeadsetOn(SingleCallActivity.this)) {
                HeadsetInfo headsetInfo = new HeadsetInfo(true, HeadsetInfo.HeadsetType.BluetoothA2dp);
                onEventMainThread(headsetInfo);
            }
        }
    }


    @Override
    public void onCallOutgoing(RCSCallSession callSession, SurfaceView localVideo) {
        super.onCallOutgoing(callSession, localVideo);
        try {
            UserInfo InviterUserIdInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
            UserInfo SelfUserInfo = RongContext.getInstance().getUserInfoFromCache(callSession.getSelfUserId());
            if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.VIDEO)) {
                mLPreviewContainer.setVisibility(View.VISIBLE);
                localVideo.setTag(callSession.getSelfUserId());
                mLPreviewContainer.addView(localVideo);
                if (null != SelfUserInfo && null != SelfUserInfo.getName()) {
                    //单人视频
                    TextView callkit_voip_user_name_signleVideo = (TextView) mUserInfoContainer.findViewById(R.id.callkit_voip_user_name_signleVideo);
                    callkit_voip_user_name_signleVideo.setText(SelfUserInfo.getName());
                }
            } else if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.AUDIO)) {
                if (null != InviterUserIdInfo && null != InviterUserIdInfo.getPortraitUri()) {
                    ImageView iv_icoming_backgroud = mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);
                    GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_icoming_backgroud, null != InviterUserIdInfo ? InviterUserIdInfo.getPortraitUri() : null);
                    iv_icoming_backgroud.setVisibility(View.VISIBLE);
                    mUserInfoContainer.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        onOutgoingCallRinging();

        regisHeadsetPlugReceiver();
        if (BluetoothUtil.hasBluetoothA2dpConnected() || BluetoothUtil.isWiredHeadsetOn(this)) {
            HeadsetInfo headsetInfo = new HeadsetInfo(true, HeadsetInfo.HeadsetType.BluetoothA2dp);
            onEventMainThread(headsetInfo);
        }
    }

    @Override
    public void onCallConnected(RCSCallSession callSession, SurfaceView localVideo) {
        super.onCallConnected(callSession, localVideo);
        if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
            ImageView button = btnLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(true);
            mButtonContainer.removeAllViews();
            mButtonContainer.addView(btnLayout);
        } else {
            mConnectionStateTextView.setVisibility(View.VISIBLE);
            mConnectionStateTextView.setText(R.string.rc_voip_connecting);
            // 二人视频通话接通后 mUserInfoContainer 中更换为无头像的布局
            mUserInfoContainer.removeAllViews();
            inflater.inflate(R.layout.rc_voip_video_call_user_info, mUserInfoContainer);
            TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
            if (userInfo != null) {
                userName.setText(userInfo.getName());
//                userName.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));//callkit_shadowcolor
                CallKitUtils.textViewShadowLayer(userName, SingleCallActivity.this);
            } else {
                userName.setText(targetId);
            }
            mLocalVideo = localVideo;
            mLocalVideo.setTag(callSession.getSelfUserId());
        }
        TextView tv_rc_voip_call_remind_info = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_call_remind_info);
        CallKitUtils.textViewShadowLayer(tv_rc_voip_call_remind_info, SingleCallActivity.this);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);
        TextView remindInfo = null;
        if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.AUDIO)) {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime);
        } else {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime_video);
        }
        if (remindInfo == null) {
            remindInfo = tv_rc_voip_call_remind_info;
        }
        setupTime(remindInfo);

        RCSCallClient.getInstance().setMute(muted);
        View muteV = mButtonContainer.findViewById(R.id.rc_voip_call_mute);
        if (muteV != null) {
            muteV.setSelected(muted);
        }

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn() || BluetoothUtil.hasBluetoothA2dpConnected()) {
            handFree = false;
            RCSCallClient.getInstance().setSpeakerEnabled(false);
            ImageView handFreeV = null;
            if (null != mButtonContainer) {
                handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
            }
            if (handFreeV != null) {
                handFreeV.setSelected(false);
                handFreeV.setEnabled(false);
                handFreeV.setClickable(false);
            }
        } else {
            RCSCallClient.getInstance().setSpeakerEnabled(handFree);
            View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
            if (handFreeV != null) {
                handFreeV.setSelected(handFree);
            }
        }
        stopRing();
    }

    @Override
    protected void onDestroy() {
        stopRing();

        super.onDestroy();
    }

    private RCSCallCommon.CallMediaType remoteMediaType;
    int userType;
    SurfaceView remoteVideo;
    String remoteUserId;

    @Override
    public void onRemoteUserJoined(final String userId, RCSCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        super.onRemoteUserJoined(userId, mediaType, userType, remoteVideo);
        this.remoteMediaType = mediaType;
        this.userType = userType;
        this.remoteVideo = remoteVideo;
        this.remoteUserId = userId;
    }

    private void changeToConnectedState(String userId, RCSCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        mConnectionStateTextView.setVisibility(View.GONE);
        if (mediaType.equals(RCSCallCommon.CallMediaType.VIDEO)) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mLPreviewContainer.setVisibility(View.VISIBLE);
            mLPreviewContainer.removeAllViews();
            remoteVideo.setTag(userId);

            mLPreviewContainer.addView(remoteVideo);
            mLPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInformationShow) {
                        hideVideoCallInformation();
                    } else {
                        showVideoCallInformation();
                        handler.sendEmptyMessageDelayed(EVENT_FULL_SCREEN, 5 * 1000);
                    }
                }
            });
            mSPreviewContainer.setVisibility(View.VISIBLE);
            mSPreviewContainer.removeAllViews();
            if (mLocalVideo != null) {
                mLocalVideo.setZOrderMediaOverlay(true);
                mLocalVideo.setZOrderOnTop(true);
                mSPreviewContainer.addView(mLocalVideo);
            }
            /** 小窗口点击事件 **/
            mSPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SurfaceView fromView = (SurfaceView) mSPreviewContainer.getChildAt(0);
                        SurfaceView toView = (SurfaceView) mLPreviewContainer.getChildAt(0);

                        mLPreviewContainer.removeAllViews();
                        mSPreviewContainer.removeAllViews();
                        fromView.setZOrderOnTop(false);
                        fromView.setZOrderMediaOverlay(false);
                        mLPreviewContainer.addView(fromView);
                        toView.setZOrderOnTop(true);
                        toView.setZOrderMediaOverlay(true);
                        mSPreviewContainer.addView(toView);
                        if (null != fromView.getTag() && !TextUtils.isEmpty(fromView.getTag().toString())) {
                            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(fromView.getTag().toString());
                            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                            if (userInfo != null) {
                                userName.setText(userInfo.getName());
                            } else {
                                userName.setText(fromView.getTag().toString());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mButtonContainer.setVisibility(View.GONE);
            mUserInfoContainer.setVisibility(View.GONE);
        }
    }

    /**
     * 当通话中的某一个参与者切换通话类型，例如由 audio 切换至 video，回调 onMediaTypeChanged。
     *
     * @param userId    切换者的 userId。
     * @param mediaType 切换者，切换后的媒体类型。
     * @param video     切换着，切换后的 camera 信息，如果由 video 切换至 audio，则为 null。
     */
    @Override
    public void onMediaTypeChanged(String userId, RCSCallCommon.CallMediaType mediaType, SurfaceView video) {
        if (RCSCallClient.getInstance().getCallSession().getSelfUserId().equals(userId)) {
            showShortToast(getString(R.string.rc_voip_switched_to_audio));
        } else {
            if (RCSCallClient.getInstance().getCallSession().getMediaType() != RCSCallCommon.CallMediaType.AUDIO) {
                RCSCallClient.getInstance().changeMediaType(RCSCallCommon.CallMediaType.AUDIO);
                RCSCallClient.getInstance().getCallSession().setMediaType(RCSCallCommon.CallMediaType.AUDIO);
                showShortToast(getString(R.string.rc_voip_remote_switched_to_audio));
            }
        }
        initAudioCallView();
        handler.removeMessages(EVENT_FULL_SCREEN);
        mButtonContainer.findViewById(R.id.rc_voip_call_mute).setSelected(muted);
    }

    @Override
    public void onNetworkReceiveLost(String userId, int lossRate) {
//        RLog.d(TAG, "onNetworkReceiveLost : userId =" + userId + "   lossRate=" + lossRate);
        isReceiveLost = lossRate > 15;
        handler.post(new Runnable() {
            @Override
            public void run() {
                refreshConnectionState();
            }
        });
    }

    @Override
    public void onNetworkSendLost(int lossRate, int delay) {
//        RLog.d(TAG, "onNetworkSendLost : rate =" + lossRate + "   delay=" + delay);
        isSendLost = lossRate > 15;
        handler.post(new Runnable() {
            @Override
            public void run() {
                refreshConnectionState();
            }
        });
    }

    @Override
    public void onFirstRemoteVideoFrame(final String userId, int height, int width) {
        RLog.i(TAG, "onFirstRemoteVideoFrame() userId = " + userId + " remoteUserId = " + remoteUserId);
        if (userId.equals(remoteUserId)) {
//            mConnectionStateTextView.setVisibility(View.GONE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeToConnectedState(userId, remoteMediaType, userType, remoteVideo);
                }
            });
        }
    }

    /**
     * 视频转语音
     **/
    private void initAudioCallView() {
        mLPreviewContainer.removeAllViews();
        mLPreviewContainer.setVisibility(View.GONE);
        mSPreviewContainer.removeAllViews();
        mSPreviewContainer.setVisibility(View.GONE);
        //显示全屏底色
        findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
        findViewById(R.id.rc_voip_audio_chat).setVisibility(View.GONE);//隐藏语音聊天按钮

        View userInfoView = inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
        TextView tv_rc_voip_call_remind_info = (TextView) userInfoView.findViewById(R.id.rc_voip_call_remind_info);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);

        TextView timeView = (TextView) userInfoView.findViewById(R.id.tv_setupTime);
        setupTime(timeView);

        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoView);
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            userName.setText(userInfo.getName());
            if (RCSCallClient.getInstance().getCallSession().getMediaType().equals(RCSCallCommon.CallMediaType.AUDIO)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null) {
                    userPortrait.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
            } else {//单人视频接听layout
                ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_large_preview);
                iv_large_preview.setVisibility(View.VISIBLE);
                GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
            }
        }
        mUserInfoContainer.setVisibility(View.VISIBLE);
        mUserInfoContainer.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);

        View button = inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(button);
        mButtonContainer.setVisibility(View.VISIBLE);
        // 视频转音频时默认不开启免提
        handFree = false;
        RCSCallClient.getInstance().setSpeakerEnabled(false);
        View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
        handFreeV.setSelected(handFree);

        ImageView iv_large_preview_Mask = (ImageView) userInfoView.findViewById(R.id.iv_large_preview_Mask);
        iv_large_preview_Mask.setVisibility(View.VISIBLE);

        /**视频切换成语音 全是语音界面的ui**/
        ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);

        if (null != userInfo && RCSCallClient.getInstance().getCallSession().getMediaType().equals(RCSCallCommon.CallMediaType.AUDIO)) {
            GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
            iv_large_preview.setVisibility(View.VISIBLE);
        }

        if (pickupDetector != null) {
            pickupDetector.register(this);
        }
    }

    private boolean hangupInvoked = false;

    public void onHangupBtnClick(View view) {
        if (hangupInvoked)
            return;
        hangupInvoked = true;
        stopRing();
        unRegisterHeadsetplugReceiver();
        RCSCallClient.getInstance().hangUpCall(hangupCallback);
    }

    private RCSSignalCallback hangupCallback = new RCSSignalCallback() {
        @Override
        public void onSuccess(RCSCallSession callSession) {
        }

        @Override
        public void onFailed(int errorCode) {
        }
    };

    private boolean acceptCallInvoked = false;

    public void onReceiveBtnClick(View view) {
        if (acceptCallInvoked)
            return;
        acceptCallInvoked = true;
        RCSCallClient.getInstance().acceptCall();
    }

    public void hideVideoCallInformation() {
        isInformationShow = false;
        mUserInfoContainer.setVisibility(View.GONE);
        mButtonContainer.setVisibility(View.GONE);
        findViewById(R.id.rc_voip_audio_chat).setVisibility(View.GONE);
    }

    public void showVideoCallInformation() {
        isInformationShow = true;
        mUserInfoContainer.setVisibility(View.VISIBLE);

        mUserInfoContainer.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
        mButtonContainer.setVisibility(View.VISIBLE);
        RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        btnLayout.findViewById(R.id.rc_voip_call_mute).setSelected(muted);
        btnLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.GONE);
        btnLayout.findViewById(R.id.rc_voip_camera).setVisibility(View.VISIBLE);
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(btnLayout);
        View view = findViewById(R.id.rc_voip_audio_chat);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RCSCallClient.getInstance().changedMediaType(RCSCallCommon.CallMediaType.AUDIO, new RCSSignalCallback() {
                    @Override
                    public void onSuccess(RCSCallSession callSession) {
                        initAudioCallView();
                    }

                    @Override
                    public void onFailed(int errorCode) {

                    }
                });
            }
        });
    }

    public void onHandFreeButtonClick(View view) {
        CallKitUtils.speakerphoneState = !view.isSelected();
        RCSCallClient.getInstance().setSpeakerEnabled(!view.isSelected());//true:打开免提 false:关闭免提
        view.setSelected(!view.isSelected());
        handFree = view.isSelected();
    }

    public void onMuteButtonClick(View view) {
        RCSCallClient.getInstance().setMute(!view.isSelected());
        view.setSelected(!view.isSelected());
        muted = view.isSelected();
    }

    @Override
    public void onCallDisconnected(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason) {
        super.onCallDisconnected(callSession, reason);

        String senderId;
        String extra = "";

        isFinishing = true;
        if (callSession == null) {
            RLog.e(TAG, "onCallDisconnected. callSession is null!");
            postRunnableDelay(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
            return;
        }
        senderId = callSession.getInviterUserId();
        switch (reason) {
            case HANGUP:
            case REMOTE_HANGUP:
                long time = getTime();
                if (time >= 3600) {
                    extra = String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60));
                } else {
                    extra = String.format("%02d:%02d", (time % 3600) / 60, (time % 60));
                }
                break;
            case OTHER_DEVICE_HAD_ACCEPTED:
                extra = getString(R.string.rc_voip_call_other);
                break;
            default:
                break;
        }
        cancelTime();

        if (!TextUtils.isEmpty(senderId)) {
            CallSTerminateMessage message = new CallSTerminateMessage();
            message.setReason(reason);
            message.setMediaType(callSession.getMediaType());
            message.setExtra(extra);
            long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
            List<String> remoteIds = callSession.getParticipantUserIds();
            String remoteId = remoteIds.size() > 0 ? remoteIds.get(0) : null;

            RLog.i(TAG, "hangup reason " + reason + " myUserId " + callSession.getSelfUserId() + " senderId " + senderId);

            if (senderId.equals(callSession.getSelfUserId())) {
                message.setDirection("MO");
                RLog.i(TAG, "insertOutgoingMessage targetId " + remoteId);
                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, remoteId, io.rong.imlib.model.Message.SentStatus.SENT, message, serverTime, null);
            } else {
                message.setDirection("MT");
                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                receivedStatus.setRead();
                RLog.i(TAG, "insertIncomingMessage targetId " + remoteId + " myId " + callSession.getSelfUserId());
                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, remoteId, callSession.getSelfUserId(), receivedStatus, message, serverTime, null);
            }
        }
        postRunnableDelay(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    private Runnable mCheckConnectionStableTask = new Runnable() {
        @Override
        public void run() {
            boolean isConnectionStable = !isSendLost && !isReceiveLost;
            if (isConnectionStable) {
                mConnectionStateTextView.setVisibility(View.GONE);
            }
        }
    };

    private void refreshConnectionState() {
        if (isSendLost || isReceiveLost) {
            if (mConnectionStateTextView.getVisibility() == View.GONE) {
                mConnectionStateTextView.setText(R.string.rc_voip_unstable_call_connection);
                mConnectionStateTextView.setVisibility(View.VISIBLE);
                if (mSoundPool != null) {
                    mSoundPool.release();
                }
                mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
                mSoundPool.load(this, R.raw.voip_network_error_sound, 0);
                mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        soundPool.play(sampleId, 1F, 1F, 0, 0, 1F);
                    }
                });
            }
            mConnectionStateTextView.removeCallbacks(mCheckConnectionStableTask);
            mConnectionStateTextView.postDelayed(mCheckConnectionStableTask, 3000);
        }
    }

    @Override
    public void onRestoreFloatBox(Bundle bundle) {
        super.onRestoreFloatBox(bundle);
        if (bundle == null)
            return;
        muted = bundle.getBoolean("muted");
        handFree = bundle.getBoolean("handFree");

        setShouldShowFloat(true);
        RCSCallSession callSession = RCSCallClient.getInstance().getCallSession();
        if (callSession == null) {
            setShouldShowFloat(false);
            finish();
            return;
        }
        RCSCallCommon.CallMediaType mediaType = callSession.getMediaType();
        RCSCallAction callAction = RCSCallAction.valueOf(getIntent().getStringExtra("callAction"));
        inflater = LayoutInflater.from(this);
        initView(mediaType, callAction);
        targetId = callSession.getTargetId();
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            userName.setText(userInfo.getName());
            if (mediaType.equals(RCSCallCommon.CallMediaType.AUDIO)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null) {
                    userPortrait.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
            } else if (mediaType.equals(RCSCallCommon.CallMediaType.VIDEO)) {
                if (null != callAction && callAction.equals(RCSCallAction.ACTION_INCOMING_CALL)) {
                    ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_large_preview);
                    iv_large_preview.setVisibility(View.VISIBLE);
                    GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
                }
            }
        }
        SurfaceView localVideo = null;
        SurfaceView remoteVideo = null;
        String remoteUserId = null;
        for (RCSCallUserProfile profile : callSession.getParticipantProfileList()) {
            if (profile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                localVideo = profile.getVideoView();
            } else {
                remoteVideo = profile.getVideoView();
                remoteUserId = profile.getUserId();
            }
        }
        if (localVideo != null && localVideo.getParent() != null) {
            ((ViewGroup) localVideo.getParent()).removeView(localVideo);
        }
        onCallOutgoing(callSession, localVideo);
        if (!(boolean) bundle.get("isDial")) {
            onCallConnected(callSession, localVideo);
        }
        if (remoteVideo != null && remoteVideo.getParent() != null) {
            ((ViewGroup) remoteVideo.getParent()).removeView(remoteVideo);
            changeToConnectedState(remoteUserId, mediaType, 1, remoteVideo);
        }
    }

    @Override
    public String onSaveFloatBoxState(Bundle bundle) {
        super.onSaveFloatBoxState(bundle);
        RCSCallSession callSession = RCSCallClient.getInstance().getCallSession();
        if (callSession == null) {
            return null;
        }
        bundle.putBoolean("muted", muted);
        bundle.putBoolean("handFree", handFree);
        bundle.putInt("mediaType", callSession.getMediaType().getValue());

        return getIntent().getAction();
    }

    public void onMinimizeClick(View view) {
        super.onMinimizeClick(view);
    }

    public void onSwitchCameraClick(View view) {
        RCSCallClient.getInstance().switchCamera();
    }

    @Override
    public void onBackPressed() {
        return;
//        RCSCallSession callSession = RCSCallManager.getInstance().getCallSession();
//        List<RCSCallUserProfile> participantProfiles = callSession.getParticipantProfileList();
//        RCSCallCommon.CallStatus callStatus = null;
//        for (RCSCallUserProfile item : participantProfiles) {
//            if (item.getMemberId().equals(callSession.getSelfUserId())) {
//                callStatus = item.getCallStatus();
//                break;
//            }
//        }
//        if (callStatus != null && callStatus.equals(RCSCallCommon.CallStatus.CONNECTED)) {
//            super.onBackPressed();
//        } else {
//            RCSCallManager.getInstance().hangUpCall(callSession.getCallId());
//        }
    }

    public void onEventMainThread(UserInfo userInfo) {
        if (isFinishing()) {
            return;
        }
        if (targetId != null && targetId.equals(userInfo.getUserId())) {
            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            if (userInfo.getName() != null)
                userName.setText(userInfo.getName());
//            AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
//            if (userPortrait != null && userInfo.getPortraitUri() != null) {
//                userPortrait.setResource(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
//            }
//            ImageView iv_large_preview=mUserInfoContainer.findViewById(R.id.iv_large_preview);
//            GlideUtils.blurTransformation(SingleCallActivity.this,iv_large_preview,null!=userInfo?userInfo.getPortraitUri():null);
        }
    }

//    @Override
//    public void showOnGoingNotification() {
//        Intent intent = new Intent(getIntent().getAction());
//        Bundle bundle = new Bundle();
//        onSaveFloatBoxState(bundle);
//        intent.putExtra("floatbox", bundle);
//        intent.putExtra("callAction", RCSCallAction.ACTION_RESUME_CALL.getName());
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationUtil.showNotification(this, "todo", "coontent", pendingIntent, CALL_NOTIFICATION_ID);
//    }

    public void onEventMainThread(HeadsetInfo headsetInfo) {
        if (headsetInfo == null || !BluetoothUtil.isForground(SingleCallActivity.this)) {
            return;
        }
        try {
            if (headsetInfo.isInsert()) {
                RCSCallClient.getInstance().setSpeakerEnabled(false);
                ImageView handFreeV = null;
                if (null != mButtonContainer) {
                    handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
                }
                if (handFreeV != null) {
                    handFreeV.setSelected(false);
                    handFreeV.setEnabled(false);
                    handFreeV.setClickable(false);
                }
                if (headsetInfo.getType() == HeadsetInfo.HeadsetType.BluetoothA2dp) {
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    am.startBluetoothSco();
                    am.setBluetoothScoOn(true);
                    am.setSpeakerphoneOn(false);
                }
            } else {
                if (headsetInfo.getType() == HeadsetInfo.HeadsetType.WiredHeadset &&
                        BluetoothUtil.hasBluetoothA2dpConnected()) {
                    return;
                }
                RCSCallClient.getInstance().setSpeakerEnabled(true);
                ImageView handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
                if (handFreeV != null) {
                    handFreeV.setSelected(true);
                    handFreeV.setEnabled(true);
                    handFreeV.setClickable(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
