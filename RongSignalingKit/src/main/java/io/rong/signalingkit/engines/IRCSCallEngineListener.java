package io.rong.signalingkit.engines;


/**
 * 引擎回调
 */
public interface IRCSCallEngineListener {
    void onJoinChannelSuccess(String channel, String mediaId, int elapsed);
    void onRejoinChannelSuccess(String channel, String mediaId, int elapsed);
    void onWarning(int warn);
    void onError(int err);
    void onApiCallExecuted(String api, int error);
    void onCameraReady();
    void onVideoStopped();
    void onAudioQuality(String mediaId, int quality, short delay, short lost);
    void onLeaveChannel(String channel);
    void onRtcStats();
    void onAudioVolumeIndication(int totalVolume);
    void onUserJoined(String mediaId, int userType);
    void onUserOffline(String mediaId, int reason);
    void onUserMuteAudio(String mediaId, boolean muted);
    void onUserMuteVideo(String mediaId, boolean muted);
    void onRemoteVideoStat(String mediaId, int delay, int receivedBitrate, int receivedFrameRate);
    void onLocalVideoStat(int sentBitrate, int sentFrameRate);
    void onFirstRemoteVideoFrame(String mediaId, int width, int height, int elapsed);
    void onFirstLocalVideoFrame(int width, int height, int elapsed);
    void onFirstRemoteVideoDecoded(String mediaId, int width, int height, int elapsed);
    void onConnectionLost();
    void onConnectionInterrupted();
    void onMediaEngineEvent(int code);
    void onVendorMessage(String mediaId, byte[] data);
    void onRefreshRecordingServiceStatus(int status);

    //channel manager
    void onWhiteBoardURL(String url);
    void onNetworkReceiveLost(int lossRate);
    void onNotifySharingScreen(boolean isSharing);
    void onNotifyDegradeNormalUserToObserver(String hostUid, String userId);
    void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status);
    void onNotifyUpgradeObserverToNormalUser(String hostUid, String userId);
    void onNotifyHostControlUserDevice(String userId, String hostId, int dType, boolean isOpen);
}
