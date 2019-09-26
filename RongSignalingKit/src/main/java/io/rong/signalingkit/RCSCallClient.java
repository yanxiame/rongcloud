package io.rong.signalingkit;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.rong.common.RLog;
import io.rong.imlib.model.Conversation;
import io.rong.signalingkit.callmanager.IRCSReceivedCallListener;
import io.rong.signalingkit.callmanager.RCSCallManager;
import io.rong.signalingkit.callmanager.RCSCallUserProfile;
import io.rong.signalingkit.callmanager.RCSSignalCallback;
import io.rong.signalingkit.engines.IRCSCallEngine;

/**
 * 兼容之前的 calllib
 */
public class RCSCallClient {
    private static final String TAG = "RCSCallClient";

    private static class SingleHolder {
        static RCSCallClient instance = new RCSCallClient();
    }

    public static RCSCallClient getInstance() {
        return SingleHolder.instance;
    }

    /**
     * 获取 CallSession
     *
     * @return
     */
    public RCSCallSession getCallSession() {
        return RCSCallManager.getInstance().getCallSession();
    }

    /**
     * 获取当前通话状态
     * @return
     */
    public RCSCallCommon.CallStatus getMyCallStatus() {
        return RCSCallManager.getInstance().getMyCallStatus();
    }

    public RCSCallUserProfile getMyCallUserProfile() {
        return RCSCallManager.getInstance().getMyCallUserProfile();
    }

    public void init(Context context) {
        RCSCallManager.getInstance().init(context);
    }

    public void setEngine(IRCSCallEngine callEngine){
        RCSCallManager.getInstance().setEngine(callEngine);
    }

    /**
     * 启动通话界面，对于离线时的通话请求，应根据mViewLoaded 判断是否呼起，此外当前CallSession是否为空来决定是否呼起
     *
     * @param context                  上下文
     * @param callSession              通话实体
     * @param startForCheckPermissions android6.0需要实时获取应用权限。
     *                                 当需要实时获取权限时，设置startForCheckPermissions为true，
     *                                 其它情况下设置为false。
     */
    public void startVoIPActivity(Context context, final RCSCallSession callSession, boolean startForCheckPermissions) {
        RLog.d(TAG, "startVoIPActivity");
        String action;
        if (callSession.getConversationType().equals(Conversation.ConversationType.DISCUSSION)
                || callSession.getConversationType().equals(Conversation.ConversationType.GROUP)
                || callSession.getConversationType().equals(Conversation.ConversationType.NONE)) {
            if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.VIDEO)) {
                action = RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIVIDEO;
            } else {
                action = RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RCSCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        } else {
            if (callSession.getMediaType().equals(RCSCallCommon.CallMediaType.VIDEO)) {
                action = RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO;
            } else {
                action = RCSVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RCSCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        }
    }

    /**
     * 监听通话呼入。
     *
     * @param receivedCallListener
     */
    public void setReceivedCallListener(IRCSReceivedCallListener receivedCallListener) {
        RCSCallManager.getInstance().setReceivedCallListener(receivedCallListener);
    }

    /**
     * 检查音视频引擎是否可用
     *
     * @param context
     * @return
     */
    public boolean isVoIPEnabled(Context context) {
        if (RCSCallManager.getInstance() != null) {
            return RCSCallManager.getInstance().isVoIPEnabled();
        }
        return false;
    }

    /**
     * 设置本地视频属性，可用此接口设置本地视频分辨率。请在connect成功之后设置。
     * 默认分辨率 480x640 15FPS
     *
     * @param profile see {@link RCSCallCommon.CallVideoProfile}
     */
    public void setVideoProfile(RCSCallCommon.CallVideoProfile profile) {
        RCSCallManager.getInstance().setVideoProfile(profile);
    }

    /**
     * 设置本地视频属性，可用此接口设置本地视频分辨率。请在connect成功之后设置。
     * 默认分辨率 480x640 15FPS
     *
     * @param profile see {@link RCSCallCommon.CallVideoProfile}
     * @param minRate 最小码率 默认分辨率 480x640下最小码率为：350
     * @param maxRate 最大码率 默认分辨率 480x640下最小码率为：1000
     */
    public void setVideoProfile(RCSCallCommon.CallVideoProfile profile, int minRate, int maxRate) {
        RCSCallManager.getInstance().setVideoProfile(profile);
        RCSCallManager.getInstance().setVideoBitRate(minRate, maxRate);
    }

    /**
     * 设置本地视频属性，是否使用美颜
     *
     * @param isEnable
     */
    public void setBeautyEnable(boolean isEnable) {
        RCSCallManager.getInstance().setBeautyEnable(isEnable);
    }

    /**
     * 接听通话
     */
    public void acceptCall() {
        RCSCallManager.getInstance().acceptCall();
    }

    /**
     * 挂断当前通话
     */
    public void hangUpCall(final RCSSignalCallback callback) {
        RCSCallManager.getInstance().hangUpCall(callback);
    }

    /**
     *
     */
    public void hangUpCall() {
        RCSCallManager.getInstance().hangUpCallWhileDestory();
    }

    /**
     * 邀请用户加入通话
     *
     * @param userIds
     */
    public void inviteRemoteUsers(List<String> userIds) {
        RCSCallManager.getInstance().addParticipants(userIds);
    }


    /**
     * 更换自己使用的媒体类型
     *
     * @param callMediaType
     */
    public void changedMediaType(RCSCallCommon.CallMediaType callMediaType, final RCSSignalCallback callback) {
        RCSCallManager.getInstance().changeCallType(callMediaType, callback);
    }

    public void changeMediaType(RCSCallCommon.CallMediaType audio) {
        RCSCallManager.getInstance().changeCallMediaType(audio);
    }

    /**
     * 设置静音状态
     *
     * @param mute
     */
    public void setMute(boolean mute) {
        RCSCallManager.getInstance().disableLocalAudioStream(mute);
    }

    /**
     * 设置扬声器状态
     *
     * @param isEnable
     */
    public void setSpeakerEnabled(boolean isEnable) {
        RCSCallManager.getInstance().setSpeakerphone(isEnable);
    }

    /**
     * 设置摄像头是否采集
     *
     * @param isEnable
     */
    public void setCameraEnabled(boolean isEnable) {
        RCSCallManager.getInstance().disableLocalVideoStream(!isEnable);
    }

    /**
     * 切换前后摄像头
     *
     */
    public void switchCamera() {
        RCSCallManager.getInstance().switchCamera();
    }

}

