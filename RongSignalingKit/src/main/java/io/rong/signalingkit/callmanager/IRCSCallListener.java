package io.rong.signalingkit.callmanager;

import android.view.SurfaceView;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.signalingkit.RCSCallSession;

public interface IRCSCallListener {
    /**
     * 电话已拨出。
     * 主叫端拨出电话后，通过回调 onCallOutgoing 通知当前 call 的详细信息。
     *
     * @param callSession 通话实体。
     * @param localVideo  本地 camera 信息。
     */
    void onCallOutgoing(RCSCallSession callSession, SurfaceView localVideo);

    /**
     * 已建立通话。
     * 通话接通时，通过回调 onCallConnected 通知当前 call 的详细信息。
     *
     * @param callSession 通话实体。
     * @param localVideo  本地 camera 信息。
     */
    void onCallConnected(RCSCallSession callSession, SurfaceView localVideo);

    /**
     * 通话结束。
     * 通话中，对方挂断，己方挂断，或者通话过程网络异常造成的通话中断，都会回调 onCallDisconnected。
     *
     * @param callSession 通话实体。
     * @param reason      通话中断原因。
     */
    void onCallDisconnected(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason);

    /**
     * 被叫端正在振铃。
     * 主叫端拨出电话，被叫端收到请求，发出振铃响应时，回调 onRemoteUserRinging。
     *
     * @param userId 振铃端用户 id。
     */
    void onRemoteUserRinging(String userId);

    /**
     * 被叫端加入通话。
     * 主叫端拨出电话，被叫端收到请求后，加入通话，回调 onRemoteUserJoined。
     *
     * @param userId      加入用户的 id。
     * @param mediaType   加入用户的媒体类型，audio or video。
     * @param userType    加入用户的类型，1:正常用户,2:观察者。
     * @param remoteVideo 加入用户者的 camera 信息。
     */
    void onRemoteUserJoined(String userId, RCSCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo);

    /**
     * 通话中的某一个参与者，邀请好友加入通话，发出邀请请求后，回调 onRemoteUserInvited。
     * @param userId 被邀请者的ID
     * @param mediaType
     */
    void onRemoteUserInvited(String userId, RCSCallCommon.CallMediaType mediaType);

    /**
     * 通话中的远端参与者离开。
     * 回调 onRemoteUserLeft 通知状态更新。
     *
     * @param userId 远端参与者的 id。
     * @param reason 远端参与者离开原因。
     */
    void onRemoteUserLeft(String userId, RCSCallCommon.CallDisconnectedReason reason);

    /**
     * 当通话中的某一个参与者切换通话类型，例如由 audio 切换至 video，回调 onMediaTypeChanged。
     *
     * @param userId    切换者的 userId。
     * @param mediaType 切换者，切换后的媒体类型。
     * @param video     切换着，切换后的 camera 信息，如果由 video 切换至 audio，则为 null。
     */
    void onMediaTypeChanged(String userId, RCSCallCommon.CallMediaType mediaType, SurfaceView video);

    /**
     * 通话过程中，发生异常。
     *
     * @param errorCode 异常原因。
     */
    void onError(RCSCallCommon.CallErrorCode errorCode);

    /**
     * 远端参与者 camera 状态发生变化时，回调 onRemoteCameraDisabled 通知状态变化。
     *
     * @param userId   远端参与者 id。
     * @param disabled 远端参与者 camera 是否可用。
     */
    void onRemoteCameraDisabled(String userId, boolean disabled);

    /**
     * 接收丢包率信息回调
     *
     * @param userId   远端用户的ID
     * @param lossRate 丟包率：0-100
     */
    void onNetworkReceiveLost(String userId, int lossRate);

    /**
     * 发送丢包率信息回调
     *
     * @param lossRate 丢包率，0-100
     * @param delay 发送端的网络延迟
     */
    void onNetworkSendLost(int lossRate, int delay);

    /**
     * 收到某个用户的第一帧视频数据
     * @param userId
     * @param height
     * @param width
     */
    void onFirstRemoteVideoFrame(String userId, int height, int width);
}
