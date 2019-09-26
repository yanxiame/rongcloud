package io.rong.signalingkit.callmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import io.rong.signal.enums.RCSignalingMessageFilterType;
import io.rong.signal.infos.RCSignalingChannelAttributeChangedNotification;
import io.rong.signal.infos.RCSignalingMemberAttributeChangedNotification;
import io.rong.signal.infos.RCSignalingMessageOption;
import io.rong.signal.interfaces.RCSignalingResultCallback;
import io.rong.signalingkit.RCSCall;
import io.rong.signalingkit.engines.IRCSCallEngine;
import io.rong.signalingkit.engines.IRCSCallEngineListener;
import io.rong.signalingkit.util.JsonUtils;
import io.rong.signalingkit.RCSCallCommon;
import io.rong.signalingkit.RCSCallSession;
import io.rong.common.RLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.signal.core.RCSignalingClient;
import io.rong.signal.infos.RCSignalingEventInfo;
import io.rong.signal.interfaces.RCSignalingEventObserver;
import io.rong.signal.messages.RCSignalingAcceptMessage;
import io.rong.signal.messages.RCSignalingCallMessage;
import io.rong.signal.messages.RCSignalingCustomMessage;
import io.rong.signal.messages.RCSignalingHangupMessage;
import io.rong.signal.messages.RCSignalingInviteMessage;
import io.rong.signal.messages.RCSignalingMessage;
import io.rong.signal.messages.RCSignalingRejectMessage;

import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.BUSY_LINE;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.CANCEL;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.HANGUP;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.INIT_VIDEO_ERROR;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.NETWORK_ERROR;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.NO_RESPONSE;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.OTHER_DEVICE_HAD_ACCEPTED;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REJECT;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_BLOCKED;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_BUSY_LINE;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_CANCEL;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_ENGINE_UNSUPPORTED;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_HANGUP;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_NETWORK_ERROR;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_NO_RESPONSE;
import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_REJECT;
import static io.rong.signalingkit.RCSCallCommon.CallStatus.CONNECTED;
import static io.rong.signalingkit.RCSCallCommon.CallStatus.IDLE;
import static io.rong.signalingkit.RCSCallCommon.CallStatus.INCOMING;
import static io.rong.signalingkit.RCSCallCommon.CallStatus.OUTGOING;
import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT;
import static io.rong.imlib.common.DeviceUtils.ShortMD5;

public class RCSCallManager implements RCSignalingEventObserver {
    public static final String TAG = "RCSCallManager";
    private static final String JSON_REASON_KEY = "reason";
    private static final String JSON_CONVERSATION_TYPE_KEY = "conversationType";
    private static final String JSON_MEDIA_TYPE_KEY = "mediaType";
    private static final String JSON_TO_USER_ID_LIST_KEY = "toUserIdList";
    private static final String JSON_USER_ID_KEY = "userId";
    private static final String JSON_TARGET_ID_KEY = "targetId";
    private static final String JSON_EXISTED_USER_PROFILES_KEY = "existedUserProfiles";
    private static final String JSON_CALL_STATUS_KEY = "callStatus";
    private static final String JSON_INVITE_USER_IDS_KEY = "inviteUserIds";
    private static final String JSON_CUSTOM_TYPE_KEY = "customType";

    public int actionIdx;
    private Context context;
    private RCSCallSession callSession;
    private Queue<RCSignalingEventInfo> hangedEvents = new LinkedList<>();
    private IRCSMissedCallListener missedCallListener;
    private IRCSReceivedCallListener receivedCallListener;
    private volatile IRCSCallEngine engine;
    private final long countDownTime = 60 * 1000;
    private final long countDownTick = 1000;
    private CountDownTimer countDownTimer;
    private Timer timeoutTimer;
    private TimerTask inviteTimeoutTask;
    private Handler uiHandler;

    public static class SingleHolder {
        static RCSCallManager instance = new RCSCallManager();
    }

    public static RCSCallManager getInstance() {
        return SingleHolder.instance;
    }

    public RCSCallManager() {
        RCSignalingClient.getInstance().addEventObserver(this);
        uiHandler = new Handler(Looper.getMainLooper());
        startTimer();
    }

    public void init(Context context) {
        this.context = context;
    }

    public void setMissedCallListener(IRCSMissedCallListener missedCallListener) {
        this.missedCallListener = missedCallListener;
    }

    public void setReceivedCallListener(IRCSReceivedCallListener receivedCallListener) {
        this.receivedCallListener = receivedCallListener;
    }

    public void setEngine(IRCSCallEngine callEngine) {
        this.engine = callEngine;
        this.engine.create(context, new RCSCallManager.EngineListener());
    }

    /**
     * 获取当前通话实体
     * @return
     */
    public RCSCallSession getCallSession() {
        return callSession;
    }

    public RCSCallUserProfile getMyCallUserProfile() {
        if (callSession == null)
            return null;
        return callSession.getCallUserProfile(RongIMClient.getInstance().getCurrentUserId());
    }

    @Override
    public void onReceiveSignalingAction(RCSignalingEventInfo info) {
        Log.i(TAG, "onReceiveSignalingAction() info: " + info);
        switch (info.getSignalingType()) {
            case CALL:
                onReceiveCall(info);
                break;
            case INVITE:
                onReceiveInvite(info);
                break;
            case HANGUP:
                onReceiveHangUp(info);
                break;
            case ACCEPT:
                onReceiveAccept(info);
                break;
            case SERVERNOTIFY:
                break;
            case CUSTOM:
                onReceiveCustom(info);
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceiveChannelAttributeChanged(RCSignalingChannelAttributeChangedNotification notification) {

    }

    @Override
    public void onReceiveMemberAttributeChanged(RCSignalingMemberAttributeChangedNotification notification) {

    }

    @Override
    public void onConnectStatusChanged(RongIMClient.ConnectionStatusListener.ConnectionStatus status) {
        RLog.e(TAG, "onConnectStatusChanged " + status);
        if (status == KICKED_OFFLINE_BY_OTHER_CLIENT) { //其他用户登录本端被踢
            if (callSession != null && getMyCallStatus() != IDLE) {
                notifyCallDisconnected(OTHER_DEVICE_HAD_ACCEPTED);
            }
        }
    }

    private void onReceiveCall(RCSignalingEventInfo info) {
        RCSCallCommon.CallStatus myCallStatus = getMyCallStatus();
        RLog.d(TAG, "current call status " + myCallStatus + " receive call " + info.getChannelId() + " from " + info.getFromUserId());
        if (myCallStatus != IDLE) {
            Map<String, Object> params = JsonUtils.jsonStrToMap(info.getSignalingContent());
            List<String> toUserIds = (List<String>) params.get(JSON_TO_USER_ID_LIST_KEY);
            toUserIds.add(info.getFromUserId());
            toUserIds.remove(RongIMClient.getInstance().getCurrentUserId());
            // 当前忙，挂断信令需要发给本请求中所有参与人
            sendHangupSignal(info.getChannelId(), toUserIds, null);
            return;
        }
        if (System.currentTimeMillis() > info.getSendTime() + countDownTime) {
            if (missedCallListener != null) {
                RCSCallSession missedCall = initSessionByCall(info);
                missedCallListener.onCallMissed(missedCall, NO_RESPONSE);
            }
            return;
        }
        // 当前会话已完成但是callSession未清空或UI层回调未完成，又来了新的会话，先挂起新会话请求
        if (callSession != null) {
            RLog.i(TAG, "hangedEvents.add(info)");
            hangedEvents.add(info);
            return;
        }
        notifyCallReceived(info);
        RLog.i(TAG, "start receiver 60s CountDownTimer");
        start60sCountDown(new Runnable() {
            @Override
            public void run() {
                if (callSession != null && !TextUtils.isEmpty(callSession.getCallId())) {
                    sendHangupSignal(callSession.getCallId(), callSession.getParticipantUserIds(), null);
                }
                notifyCallDisconnected(NO_RESPONSE);
            }
        });
    }

    private synchronized void notifyCallReceived(RCSignalingEventInfo info) {
        if (receivedCallListener != null) {
            RLog.i(TAG, "receivedCallListener != null");
            callSession = initSessionByCall(info);
            callSession.setStartTime(System.currentTimeMillis());
            updateUserStatus(RongIMClient.getInstance().getCurrentUserId(), INCOMING);
            updateUserStatus(info.getFromUserId(), CONNECTED);
            receivedCallListener.onReceivedCall(callSession);
            RLog.i(TAG, "notifyCallReceived");
        }
    }

    private void onReceiveInvite(RCSignalingEventInfo info) {
        // 当前会话中，收到新会话请求
        RCSCallCommon.CallStatus myCallStatus = getMyCallStatus();
        RLog.d(TAG, "current call status " + myCallStatus);
        if (myCallStatus != IDLE) {
            if (TextUtils.equals(info.getChannelId(), callSession.getCallId())) {
                Map<String, Object> params = JsonUtils.jsonStrToMap(info.getSignalingContent());
                if (params == null) {
                    return;
                }
                List<String> inviteUserIds = (List<String>) params.get(JSON_INVITE_USER_IDS_KEY);
                if (inviteUserIds == null) {
                    return;
                }
                for (String inviteUserId : inviteUserIds) {
                    if (callSession.getParticipantUserIds().contains(inviteUserId))
                        continue;
                    RCSCallUserProfile callUserProfile = new RCSCallUserProfile(inviteUserId, inviteUserId, INCOMING, callSession.getMediaType());
                    callSession.addParticipantUser(callUserProfile);

                    RCSCallListenerProxy.getInstance().onRemoteUserInvited(inviteUserId, callSession.getMediaType());
                }
            } else {
                sendHangupSignal(info.getChannelId(), info.getFromUserId(), null);
                return;
            }
            return;
        }

        if (System.currentTimeMillis() > info.getSendTime() + countDownTime) {
            if (missedCallListener != null) {
                RCSCallSession missedCall = initSessionByInvite(info);
                missedCallListener.onCallMissed(missedCall, NO_RESPONSE);
            }
            return;
        }
        // 当前会话已完成但是callSession未清空或UI层回调未完成，又来了新的会话，先挂起新会话请求
        if (callSession != null) {
            hangedEvents.add(info);
            return;
        }
        if (receivedCallListener != null) {
            callSession = initSessionByInvite(info);
            callSession.setStartTime(System.currentTimeMillis());
            updateUserStatus(RongIMClient.getInstance().getCurrentUserId(), INCOMING);
            updateUserStatus(info.getFromUserId(), CONNECTED);
            receivedCallListener.onReceivedCall(callSession);
        }
        RLog.i(TAG, "start receiver 60s CountDownTimer");
        start60sCountDown(new Runnable() {
            @Override
            public void run() {
                if (callSession != null && !TextUtils.isEmpty(callSession.getCallId())) {
                    sendHangupSignal(callSession.getCallId(), callSession.getParticipantUserIds(), null);
                }
                notifyCallDisconnected(NO_RESPONSE);
            }
        });
    }

    private void onReceiveCustom(RCSignalingEventInfo info) {
        Map<String, Object> params = JsonUtils.jsonStrToMap(info.getSignalingContent());
        if (info == null || callSession == null || info.getSignalingContent() == null || info.getSignalingContent().isEmpty() ||
                !params.containsKey(JSON_CUSTOM_TYPE_KEY) || !params.containsKey(JSON_MEDIA_TYPE_KEY)) {
            return;
        }
        int iCustomType = (int) params.get(JSON_CUSTOM_TYPE_KEY);
        int iMediaType = (int) params.get(JSON_MEDIA_TYPE_KEY);
        RCSCallCommon.CallMediaType toMediaType = RCSCallCommon.CallMediaType.valueOf(iMediaType);
        if (iCustomType == 1 && callSession.getMediaType() == RCSCallCommon.CallMediaType.VIDEO && toMediaType == RCSCallCommon.CallMediaType.AUDIO) {
            RCSCallListenerProxy.getInstance().onMediaTypeChanged(info.getFromUserId(), RCSCallCommon.CallMediaType.AUDIO, callSession.getCallUserProfile(info.getFromUserId()).getVideoView());
        }
    }

    private void onReceiveHangUp(RCSignalingEventInfo info) {
        if (callSession == null) {
            return;
        }
        RCSCallUserProfile profile = callSession.getCallUserProfile(info.getFromUserId());
        // 收到HANGUP信令，需要判断当前callId是否相同
        if (profile == null || !TextUtils.equals(callSession.getCallId(), info.getChannelId())) {
            return;
        }
        switch (getMyCallStatus()) {
            case CONNECTED:
                onLeave(info);
                break;
            case INCOMING:
            case OUTGOING:
                onRemoteReject(info);
                break;
            default:
                break;
        }
    }

    private void onRemoteReject(RCSignalingEventInfo info) {
        Map<String, Object> params = JsonUtils.jsonStrToMap(info.getSignalingContent());

        RCSCallCommon.CallDisconnectedReason reason = getHangupReasonFromParas(params);
        if (callSession != null) {
            if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
                RLog.i(TAG, "onRemoteReject PRIVATE stop60sCountDown");
                notifyCallDisconnected(reason);
            } else if (callSession.getConversationType() == Conversation.ConversationType.GROUP) {
                removeUser(info.getFromUserId());
                if (callSession.getParticipantProfileList().size() > 1) {
                    RCSCallListenerProxy.getInstance().onRemoteUserLeft(info.getFromUserId(), reason);
                } else if (callSession.getParticipantProfileList().size() == 1) { // 仅有自己
                    RLog.i(TAG, "onRemoteReject stop60sCountDown");
                    notifyCallDisconnected(reason);
                }
            }
        }
    }

    private void leaveChannel() {
        if (engine != null) {
            engine.leaveChannel();
            engine.muteLocalAudioStream(true);
            engine.muteLocalVideoStream(true);
            engine.disableVideo();
        }
    }

    private void onLeave(RCSignalingEventInfo info) {
        Map<String, Object> params = JsonUtils.jsonStrToMap(info.getSignalingContent());
        RCSCallCommon.CallDisconnectedReason reason = getHangupReasonFromParas(params);
        handleUserOffline(info.getFromUserId(), reason);
    }

    private void handleUserOffline(String userId, RCSCallCommon.CallDisconnectedReason reason) {
        RLog.d(TAG, "handleUserOffline remoteId " + userId + " REASON " + reason);
        if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
            notifyCallDisconnected(reason);
        } else if (callSession.getConversationType() == Conversation.ConversationType.GROUP) {
            if (callSession.getParticipantUserIds().contains(userId)) {
                removeUser(userId);
                RCSCallListenerProxy.getInstance().onRemoteUserLeft(userId, reason);
            }
            if (callSession.getParticipantProfileList().size() == 1) { // 仅有自己
                notifyCallDisconnected(reason);
            }
        }
    }

    private void onReceiveAccept(RCSignalingEventInfo info) {
        RLog.i(TAG, "onReceiveAccept stop60sCountDown");
        if (callSession == null || !callSession.getParticipantUserIds().contains(info.getFromUserId())) {
            return;
        }

        if (getMyCallStatus() == OUTGOING) {
            // 收到远端接收信令，设置远端对应用户CallStatus为connected，并根据具体情况决定是否为其创建VideoView
            stop60sCountDown();
            callSession.setActiveTime(System.currentTimeMillis());
        }

        updateUserStatus(info.getFromUserId(), CONNECTED);
        initVideoViews();

        if (getMyCallStatus() == OUTGOING) {
            // 已方发起呼叫，对方接受，设置更新自己CallStatus为CONNECTED
            updateUserStatus(RongIMClient.getInstance().getCurrentUserId(), CONNECTED);
            RCSCallListenerProxy.getInstance().onCallConnected(callSession, getMyCallUserProfile().getVideoView());
        }
        String userId = info.getFromUserId();
        RCSCallUserProfile profile = callSession.getCallUserProfile(userId);

        if (getMyCallStatus() == CONNECTED) {
            /**
             * 己方未接听时，无须通知onRemoteUserJoined。
             * 对应用户VideoView未初始化成功，回调onRemoteUserJoined UI会有crash
             * 由已发Accept之后，再统一补偿
             */
            RCSCallListenerProxy.getInstance().onRemoteUserJoined(userId, profile.getMediaType(), profile.getUserType().getValue(), profile.getVideoView());
        }
    }

    /**
     * 发起呼叫
     *
     * @param conversationType
     * @param targetId
     * @param userIds
     * @param mediaType
     */
    public void startCall(Conversation.ConversationType conversationType,
                          String targetId, List<String> userIds, RCSCallCommon.CallMediaType mediaType) {
        String myUserId = RongIMClient.getInstance().getCurrentUserId();
        Log.i(TAG, "myUserId = " + myUserId);
        callSession = new RCSCallSession();
        callSession.setCallId(makeCallId(conversationType, targetId, userIds));
        callSession.setMediaType(mediaType);
        callSession.setUserType(RCSCallCommon.CallUserType.NORMAL);
        callSession.setConversationType(conversationType);
        callSession.setTargetId(targetId);
        callSession.setCallerUserId(myUserId);
        callSession.setEngineType(RCSCallEngineConfig.EnginePreferred);
        callSession.setUserType(RCSCallCommon.CallUserType.NORMAL);
        callSession.setInviterUserId(myUserId);
        callSession.setStartTime(System.currentTimeMillis());

        callSession.addParticipantUser(new RCSCallUserProfile(RongIMClient.getInstance().getCurrentUserId(), RongIMClient.getInstance().getCurrentUserId(), OUTGOING, mediaType));
        for (String id : userIds) {
            if (TextUtils.equals(RongIMClient.getInstance().getCurrentUserId(), id))
                continue;
            callSession.addParticipantUser(new RCSCallUserProfile(id, id, INCOMING, mediaType));
        }

        initVideoViews();
        if (callSession.getMediaType() == RCSCallCommon.CallMediaType.VIDEO) {
            engine.enableVideo();
            engine.startPreview();
        } else {
            engine.disableVideo();
            engine.stopPreview();
        }
        RCSCallListenerProxy.getInstance().onCallOutgoing(callSession, getMyCallUserProfile().getVideoView());
        engine.joinChannel(callSession.getCallId(), "call", RongIMClient.getInstance().getCurrentUserId());//加入房间
        RLog.i(TAG, "start caller 60s CountDownTimer");
        start60sCountDown(new Runnable() {
            @Override
            public void run() {
                if (callSession != null && !TextUtils.isEmpty(callSession.getCallId())) {
                    sendHangupSignal(callSession.getCallId(), callSession.getParticipantUserIds(), null);
                }
                notifyCallDisconnected(REMOTE_NO_RESPONSE);
            }
        });
    }

    private void initVideoViews() {
        // 当前无会话、engine为初始化、纯音频通话，不创建用户VideoView
        if (callSession == null || engine == null || callSession.getMediaType() == RCSCallCommon.CallMediaType.AUDIO)
            return;
        for (RCSCallUserProfile profile : callSession.getParticipantProfileList()) {
            if (profile.getVideoView() != null && profile.getVideoSetup())
                continue;
            if (profile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                SurfaceView surfaceView = engine.createRendererView(context);
                profile.setVideoView(surfaceView);
                engine.setupLocalVideo(surfaceView);
                profile.setVideoSetup(true);
            } else if (profile.getCallStatus() == CONNECTED) {
                SurfaceView surfaceView;
                if (profile.getVideoView() == null) {
                    surfaceView = engine.createRendererView(context);
                    profile.setVideoView(surfaceView);
                } else {
                    surfaceView = profile.getVideoView();
                }
                profile.setVideoSetup(engine.setupRemoteVideo(surfaceView, profile.getMediaId()));
            }
        }
    }

    /**
     * 发送信令
     */
    private void sendCallSignal() {
        RLog.i(TAG, "sendCallSignal()");
        if (callSession == null) {
            RLog.i(TAG, "sendCallSignal callSession is null");
            return;
        }
        final List<String> userIds = callSession.getParticipantUserIds();
        HashMap<String, Object> params = buildCallParameters(callSession.getTargetId(), userIds);

        RCSignalingMessageOption option = new RCSignalingMessageOption(RCSignalingMessageFilterType.RCMessageFilterTypeNone);
        if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
            option = new RCSignalingMessageOption(RCSignalingMessageFilterType.RCMessageFilterTypeBlackWhitelist);
        }

        final RCSignalingCallMessage callAction = new RCSignalingCallMessage(String.valueOf(actionIdx++),
                callSession.getCallId(), JsonUtils.mapToString(params), RCSCall.getAppLocalizedName());

        RCSignalingClient.getInstance().sendSignalingMessage(callAction, userIds, option, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(callAction);
                if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
                    RCSCallListenerProxy.getInstance().onRemoteUserRinging(userIds.get(0));
                }
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(callAction, errorCode);
                if (errorCode == RongIMClient.ErrorCode.REJECTED_BY_BLACKLIST.getValue()) {
                    notifyCallDisconnected(REMOTE_BLOCKED);
                } else {
                    notifyCallDisconnected(NETWORK_ERROR);
                }
            }
        });
    }

    public void acceptCall() {
        RLog.d(TAG, "acceptCall()");
        if (callSession == null) {
            notifyCallDisconnected(REMOTE_HANGUP);
            return;
        }
        if (!isNetWorkAvailable()) {
            RLog.d(TAG, "networkUnavailable HANGUP");
            notifyCallDisconnected(HANGUP);
            return;
        }

        initVideoViews();
        if (callSession.getMediaType() == RCSCallCommon.CallMediaType.VIDEO) {
            engine.enableVideo();
            engine.startPreview();
        } else {
            engine.disableVideo();
            engine.stopPreview();
        }
        RLog.i(TAG, "engine.joinChannel callId" + callSession.getCallId());
        engine.joinChannel(callSession.getCallId(), "accept", RongIMClient.getInstance().getCurrentUserId());//加入房间
    }

    public void addParticipants(List<String> invited) {
        sendInviteSignal(invited, callSession.getMediaType());
    }

    public void changeCallMediaType(RCSCallCommon.CallMediaType mediaType) {
        if (engine == null) return;
        if (mediaType == RCSCallCommon.CallMediaType.AUDIO) {
            engine.disableVideo();
            callSession.setMediaType(RCSCallCommon.CallMediaType.AUDIO);
        }
    }

    public void changeCallType(RCSCallCommon.CallMediaType toMediaType, final RCSSignalCallback callback) {
        sendSwitchCallTypeSignal(toMediaType, callSession.getCallId(), callback);
    }

    public void switchCamera() {
        if (engine == null) return;
        engine.switchCamera();
    }

    public void setSpeakerphone(boolean enable) {
        if (engine == null) return;
        engine.setEnableSpeakerphone(enable);
    }

    public void setVideoProfile(RCSCallCommon.CallVideoProfile profile) {
        if (engine == null) return;
        engine.setVideoProfile(profile.name());
    }

    public void setVideoBitRate(int minRate, int maxRate) {
        if (engine == null) return;
        engine.setVideoBitRate(minRate, maxRate);
    }

    public void disableLocalAudioStream(boolean disabled) {
        if (engine == null) return;
        engine.muteLocalAudioStream(disabled);
    }

    public void disableLocalVideoStream(boolean disabled) {
        if (engine == null) return;
        engine.muteLocalVideoStream(disabled);
    }

    private void sendAcceptCall() {
        RLog.i(TAG, "sendAcceptCall");
        List<String> userIds = callSession.getParticipantUserIds();
        final RCSignalingAcceptMessage acceptAction = new RCSignalingAcceptMessage(String.valueOf(actionIdx++), callSession.getCallId(), null);
        RCSignalingClient.getInstance().sendSignalingMessage(acceptAction, userIds, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(acceptAction);
                stop60sCountDown();
                callSession.setActiveTime(System.currentTimeMillis());
                // 保存当前自己通话状态
                RCSCallCommon.CallStatus myCallStatus = getMyCallStatus();
                // 更改当前为接通状态
                updateUserStatus(RongIMClient.getInstance().getCurrentUserId(), CONNECTED);
                // 更新
                initVideoViews();

                if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
                    RCSCallListenerProxy.getInstance().onCallConnected(callSession, getMyCallUserProfile().getVideoView());
                } else if (callSession.getConversationType() == Conversation.ConversationType.GROUP) {
                    if (myCallStatus == INCOMING) {
                        RCSCallListenerProxy.getInstance().onCallConnected(callSession, getMyCallUserProfile().getVideoView());
                    }
                }
                notifyRemoteJoined();
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(acceptAction, errorCode);
            }
        });
        RLog.i(TAG, "sendAcceptCall stop60sCountDown");
    }

    private void sendInviteSignal(final List<String> userIds, RCSCallCommon.CallMediaType mediaType) {
        for (String id : userIds) {
            RCSCallUserProfile profile = new RCSCallUserProfile(id, id, IDLE, mediaType);
            callSession.addParticipantUser(profile);
            initVideoViews();
        }

        final RCSignalingInviteMessage inviteAction = new RCSignalingInviteMessage(String.valueOf(actionIdx++),
                callSession.getCallId(), JsonUtils.mapToString(buildInviteParams(callSession.getTargetId(), userIds)), RCSCall.getAppLocalizedName());
        RCSignalingMessageOption option = new RCSignalingMessageOption(RCSignalingMessageFilterType.RCMessageFilterTypeNone);
        if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
            option = new RCSignalingMessageOption(RCSignalingMessageFilterType.RCMessageFilterTypeBlackWhitelist);
        }

        RCSignalingClient.getInstance().sendSignalingMessage(inviteAction, callSession.getParticipantUserIds(), option, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(inviteAction);
                for (String inviteUserId : userIds) {
                    RCSCallListenerProxy.getInstance().onRemoteUserInvited(inviteUserId, callSession.getMediaType());
                }
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(inviteAction, errorCode);
            }
        });
    }

    public void hangUpCall(final RCSSignalCallback callback) {
        RLog.i(TAG, "sendRejectSignal stop60sCountDown");
        stop60sCountDown();
        if (callSession == null) {
            notifyCallDisconnected(REMOTE_HANGUP);
            callback.onFailed(-1);
            return;
        }
        sendHangupSignal(callSession.getCallId(), callSession.getParticipantUserIds(), callback);
    }

    /**
     * app 被清除时，发送挂断信令
     */
    public void hangUpCallWhileDestory() {
        if (callSession == null) {
            return;
        }
        sendHangupSignal(callSession.getCallId(), callSession.getParticipantUserIds(), null);
    }

    public void setBeautyEnable(boolean beautyEnable) {
        if (engine == null)
            return;
        engine.setBeautyEnable(beautyEnable);
    }

    private void sendHangupSignal(final String callId, final String toUserId, final RCSSignalCallback callback) {
        List<String> toUserIds = new ArrayList<>();
        toUserIds.add((toUserId));

        sendHangupSignal(callId, toUserIds, callback);
    }

    private void sendHangupSignal(final String callId, final List<String> toUserIds, final RCSSignalCallback callback) {
        final RCSCallCommon.CallDisconnectedReason hangupReason = getHangupReason(callId);
        RLog.d(TAG, "hangUpCall to " + callId + " reason " + hangupReason.name());

        final RCSignalingHangupMessage hangupAction = new RCSignalingHangupMessage(String.valueOf(actionIdx++), callId, JsonUtils.mapToString(buildReasonParams(hangupReason)));
        RCSignalingClient.getInstance().sendSignalingMessage(hangupAction, toUserIds, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(hangupAction);
                if (callback != null) {
                    callback.onSuccess(callSession);
                }
                if (callSession == null)
                    return;
                // 挂断非当前进行会话，无需释放引擎、CallSession及更新UI
                if (!TextUtils.equals(callId, callSession.getCallId())) {
                    return;
                }
                notifyCallDisconnected(hangupReason);
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(hangupAction, errorCode);
                if (callback != null) {
                    callback.onFailed(errorCode);
                }
            }
        });
    }

    /**
     * 拒绝邀请
     */
    public void sendRejectSignal(RCSCallCommon.CallDisconnectedReason reason, final RCSSignalCallback callback) {
        sendRejectSignal(reason, callSession.getCallId(), callback);
        callSession = null;
        RLog.i(TAG, "sendRejectSignal stop60sCountDown");
        stop60sCountDown();
    }

    private void sendRejectSignal(final RCSCallCommon.CallDisconnectedReason reason, String callId, final RCSSignalCallback callback) {
        List<String> userIds = callSession.getParticipantUserIds();
        final RCSignalingRejectMessage rejectAction = new RCSignalingRejectMessage(String.valueOf(actionIdx++),
                callId, JsonUtils.mapToString(buildReasonParams(reason)));
        callSession.setEndTime(System.currentTimeMillis());
        RCSignalingClient.getInstance().sendSignalingMessage(rejectAction, userIds, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(rejectAction);
                if (callback != null) {
                    callback.onSuccess(callSession);
                }
                notifyCallDisconnected(reason);
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(rejectAction, errorCode);
                if (callback != null) {
                    callback.onFailed(errorCode);
                }
            }
        });
    }

    private void sendSwitchCallTypeSignal(final RCSCallCommon.CallMediaType toMediaType, String callId, final RCSSignalCallback callback) {
        List<String> userIds = callSession.getParticipantUserIds();
        Map<String, Object> map = buildSwitchCallTypeParameters(toMediaType);
        final RCSignalingCustomMessage switchCallSignal = new RCSignalingCustomMessage(String.valueOf(actionIdx++),
                callId, JsonUtils.mapToString(map), null);
        RCSignalingClient.getInstance().sendSignalingMessage(switchCallSignal, userIds, new RCSignalingResultCallback() {
            @Override
            public void onSuccess() {
                sendSuccess(switchCallSignal);
                if (engine == null) return;
                if (toMediaType == RCSCallCommon.CallMediaType.AUDIO) {
                    callSession.setMediaType(RCSCallCommon.CallMediaType.AUDIO);
                    engine.disableVideo();
                } else {
                    callSession.setMediaType(RCSCallCommon.CallMediaType.VIDEO);
                    engine.enableVideo();
                }
                if (callback != null) {
                    callback.onSuccess(callSession);
                }
            }

            @Override
            public void onFailed(int errorCode) {
                sendFailed(switchCallSignal, errorCode);
                if (callback != null) {
                    callback.onFailed(errorCode);
                }
            }
        });
    }

    private HashMap<String, Object> buildCallParameters(String targetId, List<String> userIds) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(JSON_CONVERSATION_TYPE_KEY, callSession.getConversationType().getValue());
        map.put(JSON_MEDIA_TYPE_KEY, callSession.getMediaType().getValue());
        map.put(JSON_TO_USER_ID_LIST_KEY, userIds);
        map.put(JSON_TARGET_ID_KEY, targetId);
        return map;
    }

    private Map<String, Object> buildSwitchCallTypeParameters(RCSCallCommon.CallMediaType toMediaType) {
        Map<String, Object> map = new HashMap<>();
        map.put(JSON_CUSTOM_TYPE_KEY, toMediaType.getValue());
        map.put(JSON_MEDIA_TYPE_KEY, toMediaType.getValue());
        return map;
    }

    private Map<String, Object> buildReasonParams(RCSCallCommon.CallDisconnectedReason reason) {
        Map<String, Object> map = new HashMap<>();
        map.put(JSON_REASON_KEY, reason.getValue());
        return map;
    }

    private Map<String, Object> buildInviteParams(String targetId, List<String> invitedUserIds) {
        Map<String, Object> inviteParams = new HashMap<>();
        inviteParams.put(JSON_CONVERSATION_TYPE_KEY, callSession.getConversationType().getValue());
        inviteParams.put(JSON_MEDIA_TYPE_KEY, callSession.getMediaType().getValue());
        inviteParams.put(JSON_TARGET_ID_KEY, targetId);

        List<Map<String, Object>> existedUsers = new ArrayList<>();
        for (RCSCallUserProfile user : callSession.getParticipantProfileList()) {
            if (invitedUserIds.contains(user.getUserId()))
                continue;

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put(JSON_USER_ID_KEY, user.getUserId());
            userProfile.put(JSON_MEDIA_TYPE_KEY, user.getMediaType().getValue());
            userProfile.put(JSON_CALL_STATUS_KEY, user.getCallStatus().getValue());
            existedUsers.add(userProfile);
        }
        inviteParams.put(JSON_EXISTED_USER_PROFILES_KEY, existedUsers);

        inviteParams.put(JSON_INVITE_USER_IDS_KEY, invitedUserIds.toArray());

        return inviteParams;
    }

    private RCSCallSession initSessionByCall(RCSignalingEventInfo info) {
        RCSCallSession callSession = new RCSCallSession();
        Map<String, Object> map = JsonUtils.jsonStrToMap(info.getSignalingContent());
        int type = (int) map.get(JSON_CONVERSATION_TYPE_KEY);
        int mediaType = (int) map.get(JSON_MEDIA_TYPE_KEY);
        Conversation.ConversationType conversationType = Conversation.ConversationType.setValue(type);
        // 单聊时将targetId替换为发送者id
        String targetId = null;
        if (conversationType != Conversation.ConversationType.PRIVATE) {
            if (map.containsKey(JSON_TARGET_ID_KEY)) {
                targetId = map.get(JSON_TARGET_ID_KEY).toString();
            }
        } else {
            targetId = info.getFromUserId();
        }
        callSession.setCallId(info.getChannelId());
        callSession.setCallerUserId(info.getFromUserId());
        callSession.setConversationType(conversationType);
        callSession.setInviterUserId(info.getFromUserId());
        callSession.setTargetId(targetId);
        callSession.setMediaType(RCSCallCommon.CallMediaType.valueOf(mediaType));

        RCSCallUserProfile callUserProfile = new RCSCallUserProfile(info.getFromUserId(), info.getFromUserId(), OUTGOING, callSession.getMediaType());
        callSession.addParticipantUser(callUserProfile);

        List<String> list = (List<String>) map.get(JSON_TO_USER_ID_LIST_KEY);
        for (String id : list) {
            RCSCallUserProfile profile = new RCSCallUserProfile(id, id, IDLE, callSession.getMediaType());

            callSession.addParticipantUser(profile);
        }

        return callSession;
    }

    private RCSCallSession initSessionByInvite(RCSignalingEventInfo info) {
        RCSCallSession callSession = new RCSCallSession();
        Map<String, Object> map = JsonUtils.jsonStrToMap(info.getSignalingContent());
        int type = (int) map.get(JSON_CONVERSATION_TYPE_KEY);
        int mediaType = (int) map.get(JSON_MEDIA_TYPE_KEY);
        Conversation.ConversationType conversationType = Conversation.ConversationType.setValue(type);
        // 单聊时将targetId替换为发送者id
        String targetId = null;
        if (conversationType != Conversation.ConversationType.PRIVATE) {
            if (map.containsKey(JSON_TARGET_ID_KEY)) {
                targetId = map.get(JSON_TARGET_ID_KEY).toString();
            }
        } else {
            targetId = info.getFromUserId();
        }

        callSession.setCallId(info.getChannelId());
        callSession.setCallerUserId(info.getFromUserId());
        callSession.setConversationType(conversationType);
        callSession.setInviterUserId(info.getFromUserId());
        callSession.setTargetId(targetId);
        callSession.setMediaType(RCSCallCommon.CallMediaType.valueOf(mediaType));

        List<HashMap<String, Object>> existedUsers = (List<HashMap<String, Object>>) map.get(JSON_EXISTED_USER_PROFILES_KEY);
        for (HashMap<String, Object> member : existedUsers) {
            String memberId = member.get(JSON_USER_ID_KEY).toString();
            RCSCallCommon.CallStatus callStatus = RCSCallCommon.CallStatus.valueOf(Integer.parseInt(member.get(JSON_CALL_STATUS_KEY).toString()));
            RCSCallCommon.CallMediaType memMediaType = RCSCallCommon.CallMediaType.valueOf(Integer.parseInt(member.get(JSON_MEDIA_TYPE_KEY).toString()));

            RCSCallUserProfile callUserProfile = new RCSCallUserProfile(memberId, memberId, callStatus, memMediaType);
            callSession.addParticipantUser(callUserProfile);
        }

        List<String> inviteUserIds = (List<String>) map.get(JSON_INVITE_USER_IDS_KEY);
        for (String id : inviteUserIds) {
            RCSCallUserProfile profile = new RCSCallUserProfile(id, id, IDLE, callSession.getMediaType());
            callSession.addParticipantUser(profile);
        }

        return callSession;
    }

    private String makeCallId(Conversation.ConversationType conversationType, String
            targetId, List<String> userIds) {
        String str = null;
        for (String id : userIds)
            str += id;
        long time = System.currentTimeMillis();

        return ShortMD5(conversationType.getName() + targetId + str + time);
    }

    private void sendSuccess(RCSignalingMessage action) {
        Log.i(TAG, "sendSuccess() action:" + action);
    }

    private void sendFailed(RCSignalingMessage action, int errorCode) {
        Log.i(TAG, "sendFailed() action:" + action + " errorCode = " + errorCode);
    }

    private void updateUserStatus(String userId, RCSCallCommon.CallStatus status) {
        if (callSession == null) {
            Log.i(TAG, "callSession is null!");
            return;
        }
        for (RCSCallUserProfile profile : callSession.getParticipantProfileList()) {
            if (TextUtils.equals(profile.getUserId(), userId)) {
                profile.setCallStatus(status);
            }
        }
    }

    /**
     * 获取我的通话状态
     *
     * @return
     */
    public RCSCallCommon.CallStatus getMyCallStatus() {
        RCSCallCommon.CallStatus mineCallStatus = IDLE;
        if (callSession == null || callSession.getParticipantProfileList() == null) {
            mineCallStatus = IDLE;
        } else {
            for (RCSCallUserProfile userProfile : callSession.getParticipantProfileList()) {
                if (userProfile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                    mineCallStatus = userProfile.getCallStatus();
                    break;
                }
            }
        }
        return mineCallStatus;
    }

    private void removeUser(String userId) {
        if (callSession == null) {
            return;
        }
        callSession.remoteUser(userId);
    }

    private void notifyRemoteJoined() {
        if (callSession != null) {
            for (String remoteUserId : callSession.getParticipantUserIds()) {
                RCSCallUserProfile callUserProfile = callSession.getCallUserProfile(remoteUserId);
                if (callUserProfile.getCallStatus() != CONNECTED)
                    continue;
                RCSCallListenerProxy.getInstance().onRemoteUserJoined(remoteUserId, callUserProfile.getMediaType(), callUserProfile.getUserType().getValue(), callUserProfile.getVideoView());
            }
        }
    }

    private String lastDisconnectCallId;

    private synchronized void notifyCallDisconnected(final RCSCallCommon.CallDisconnectedReason reason) {
        RLog.i(TAG, "notifyCallDisconnected: " + reason);
        // NOTE: callSession 判空直接return，在离线时收到呼叫，会造成接听页面挂不断
        if (callSession != null) {
            callSession.setEndTime(System.currentTimeMillis());
        }
        updateUserStatus(RongIMClient.getInstance().getCurrentUserId(), IDLE);
        if (callSession != null && TextUtils.equals(lastDisconnectCallId, callSession.getCallId())) {
            return;
        }
        lastDisconnectCallId = callSession == null ? null : callSession.getCallId();
        stop60sCountDown();
        leaveChannel();

        if (!RCSCallListenerProxy.getInstance().getCallListeners().isEmpty()) {
            RCSCallListenerProxy.getInstance().onCallDisconnected(callSession, reason);
        } else {
            if (missedCallListener != null) {
                missedCallListener.onCallMissed(callSession, reason);
            }
        }

        callSession = null;
        lastDisconnectCallId = null;
        // 本次会话结束后，查看缓存队列是否又未处理会话请求
        if (hangedEvents.peek() != null) {
            RLog.e(TAG, "reQueue eventInfo ");
            onReceiveSignalingAction(hangedEvents.poll());
        }
    }

    /**
     * 从 RCSignalingEventInfo 参数列表中读取挂断原因
     *
     * @param parameters
     * @return
     */
    private RCSCallCommon.CallDisconnectedReason getHangupReasonFromParas(Map<String, Object> parameters) {
        if (!parameters.containsKey(JSON_REASON_KEY))
            return NO_RESPONSE;
        int reason = (int) parameters.get(JSON_REASON_KEY);
        RCSCallCommon.CallDisconnectedReason disReason = RCSCallCommon.CallDisconnectedReason.valueOf(reason);
        // 发送方的挂断原因相对于接收方来说都是REMOTE_XXX
        switch (disReason) {
            case CANCEL:
                return REMOTE_CANCEL;
            case REJECT:
                return REMOTE_REJECT;
            case HANGUP:
                return REMOTE_HANGUP;
            case BUSY_LINE:
                return REMOTE_BUSY_LINE;
            case NO_RESPONSE:
                return REMOTE_NO_RESPONSE;
            case REMOTE_NO_RESPONSE:
                return NO_RESPONSE;
            case ENGINE_UNSUPPORTED:
                return REMOTE_ENGINE_UNSUPPORTED;
            case NETWORK_ERROR:
                return REMOTE_NETWORK_ERROR;

            case INIT_VIDEO_ERROR:
                return INIT_VIDEO_ERROR;
            case OTHER_DEVICE_HAD_ACCEPTED:
                return OTHER_DEVICE_HAD_ACCEPTED;

            default:
                return NO_RESPONSE;
        }
    }

    /**
     * 更具当前信息获取挂断原因
     *
     * @param callId
     * @return
     */
    private RCSCallCommon.CallDisconnectedReason getHangupReason(String callId) {
        RCSCallCommon.CallDisconnectedReason hangupReason = REJECT;
        RCSCallCommon.CallStatus myStatus = getMyCallStatus();
        if (callSession != null && myStatus == CONNECTED) {
            hangupReason = HANGUP;
        } else if (callSession != null && myStatus == OUTGOING) {
            hangupReason = CANCEL;
            // 主叫对方接听超时
            if (callSession.getStartTime() + countDownTime < System.currentTimeMillis()) {
                hangupReason = REMOTE_NO_RESPONSE;
            }
        } else if (callSession != null && myStatus == INCOMING) {
            hangupReason = REJECT;
            // 被叫未接听
            if (callSession.getStartTime() + countDownTime < System.currentTimeMillis()) {
                hangupReason = NO_RESPONSE;
            }
        }
        if (callSession != null && !TextUtils.equals(callId, callSession.getCallId())) {
            hangupReason = BUSY_LINE;
        }
        return hangupReason;
    }

    private void checkMemberInviteTimeout() {
        if (callSession == null || callSession.getParticipantProfileList().isEmpty() || callSession.getConversationType() == Conversation.ConversationType.PRIVATE)
            return;

        Iterator<RCSCallUserProfile> iterator = callSession.getParticipantProfileList().iterator();
        while (iterator.hasNext()) {
            RCSCallUserProfile profile = iterator.next();
            if (TextUtils.equals(profile.getUserId(), RongIMClient.getInstance().getCurrentUserId()) || profile.getCallStatus() == CONNECTED) {
                continue;
            }
            if (System.currentTimeMillis() > profile.getInviteTime() + countDownTime) {
                RLog.d(TAG, "checkMemberInviteTimeout user " + profile.getUserId());
                final String timeoutUserId = profile.getUserId();
                removeUser(timeoutUserId);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callSession.getParticipantProfileList().size() > 1) {
                            RCSCallListenerProxy.getInstance().onRemoteUserLeft(timeoutUserId, REMOTE_NO_RESPONSE);
                        } else if (callSession.getParticipantProfileList().size() == 1) { // 仅剩自己
                            RLog.i(TAG, "onRemoteReject stop60sCountDown");
                            notifyCallDisconnected(REMOTE_NO_RESPONSE);
                        }
                    }
                });
                break;
            }
        }
    }

    private void startTimer() {
        timeoutTimer = new Timer();
        inviteTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                checkMemberInviteTimeout();
            }
        };
        RLog.d(TAG, "timeoutTimer.scheduled");
        timeoutTimer.schedule(inviteTimeoutTask, countDownTick * 5, countDownTick);
    }

    private void start60sCountDown(final Runnable finishAction) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(countDownTime, countDownTick) {
            @Override
            public void onTick(long millisUntilFinished) {
//                RLog.i(TAG, millisUntilFinished + " milliSeconds left");
            }

            @Override
            public void onFinish() {
                if (countDownTimer == null)
                    return;
                countDownTimer.cancel();
                countDownTimer = null;
                if (finishAction != null)
                    finishAction.run();
            }
        };
        countDownTimer.start();
    }

    private void stop60sCountDown() {
        if (countDownTimer == null)
            return;
        countDownTimer.cancel();
        countDownTimer = null;
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getActiveNetworkInfo();
            RLog.d(TAG, "network : " + (networkInfo != null ? (networkInfo.isAvailable() + " " + networkInfo.isConnected()) : "null"));
        } catch (Exception e) {
            RLog.e(TAG, "getActiveNetworkInfo Exception", e);
        }
        boolean isNetWorkAvailable = networkInfo != null && (networkInfo.isAvailable() && networkInfo.isConnected());
        return isNetWorkAvailable;
    }

    public boolean isVoIPEnabled() {
        return getMyCallStatus() == IDLE;
    }

    private class EngineListener implements IRCSCallEngineListener {

        @Override
        public void onJoinChannelSuccess(String channel, String mediaId, int elapsed) {
            switch (getMyCallStatus()) {
                case OUTGOING:
                    sendCallSignal(); //发送呼叫信令
                    break;
                case INCOMING:
                    sendAcceptCall();//发送接收信令
                    break;
            }
        }

        @Override
        public void onRejoinChannelSuccess(String channel, String mediaId, int elapsed) {

        }

        @Override
        public void onWarning(int warn) {

        }

        @Override
        public void onError(int err) {

        }

        @Override
        public void onApiCallExecuted(String api, int error) {

        }

        @Override
        public void onCameraReady() {

        }

        @Override
        public void onVideoStopped() {

        }

        @Override
        public void onAudioQuality(String mediaId, int quality, short delay, short lost) {

        }

        @Override
        public void onLeaveChannel(String channel) {
            if (callSession == null || TextUtils.equals(channel, callSession.getCallId())) {
                return;
            }
            RLog.d(TAG, "onLeaveChannel HANGUP");
            notifyCallDisconnected(HANGUP);
        }

        @Override
        public void onRtcStats() {

        }

        @Override
        public void onAudioVolumeIndication(int totalVolume) {

        }

        @Override
        public void onUserJoined(String mediaId, int userType) {
            initVideoViews();
        }

        @Override
        public void onUserOffline(String mediaId, int reason) {
            handleUserOffline(mediaId, RCSCallCommon.CallDisconnectedReason.valueOf(reason));
        }

        @Override
        public void onUserMuteAudio(String mediaId, boolean muted) {

        }

        @Override
        public void onUserMuteVideo(String mediaId, boolean muted) {
            RCSCallListenerProxy.getInstance().onRemoteCameraDisabled(mediaId, muted);
        }

        @Override
        public void onRemoteVideoStat(String mediaId, int delay, int receivedBitrate, int receivedFrameRate) {

        }

        @Override
        public void onLocalVideoStat(int sentBitrate, int sentFrameRate) {

        }

        @Override
        public void onFirstRemoteVideoFrame(String mediaId, int width, int height, int elapsed) {
            if (TextUtils.isEmpty(mediaId) || TextUtils.equals(mediaId, RongIMClient.getInstance().getCurrentUserId()))
                return;
            RLog.i(TAG, "onFirstRemoteVideoFrame: " + mediaId);
            initVideoViews();
            RCSCallListenerProxy.getInstance().onFirstRemoteVideoFrame(mediaId, width, height);
        }

        @Override
        public void onFirstLocalVideoFrame(int width, int height, int elapsed) {

        }

        @Override
        public void onFirstRemoteVideoDecoded(String mediaId, int width, int height, int elapsed) {
            if (TextUtils.isEmpty(mediaId))
                return;
            RLog.i(TAG, "onFirstRemoteVideoDecoded: " + mediaId);
            initVideoViews();
            RCSCallListenerProxy.getInstance().onFirstRemoteVideoFrame(mediaId, width, height);
        }

        @Override
        public void onConnectionLost() {

        }

        @Override
        public void onConnectionInterrupted() {

        }

        @Override
        public void onMediaEngineEvent(int code) {

        }

        @Override
        public void onVendorMessage(String mediaId, byte[] data) {

        }

        @Override
        public void onRefreshRecordingServiceStatus(int status) {

        }

        @Override
        public void onWhiteBoardURL(String url) {

        }

        @Override
        public void onNetworkReceiveLost(int lossRate) {

        }

        @Override
        public void onNotifySharingScreen(boolean isSharing) {

        }

        @Override
        public void onNotifyDegradeNormalUserToObserver(String hostUid, String userId) {

        }

        @Override
        public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {

        }

        @Override
        public void onNotifyUpgradeObserverToNormalUser(String hostUid, String userId) {

        }

        @Override
        public void onNotifyHostControlUserDevice(String userId, String hostId, int dType, boolean isOpen) {

        }
    }
}