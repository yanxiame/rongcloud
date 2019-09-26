package io.rong.signalingkit.engines;

import android.content.Context;
import android.view.SurfaceView;


/**
 * 引擎接口类
 */
public interface IRCSCallEngine {

    void create(Context context, IRCSCallEngineListener engineListener);

    void destroy();

    SurfaceView createRendererView(Context context);

    void setupLocalVideo(SurfaceView localVideo);
    //mediaId ->userId
    boolean setupRemoteVideo(SurfaceView remoteVideo, String mediaId);

    int enableVideo();

    int disableVideo();

    int startPreview();

    int stopPreview();

    int joinChannel(String channelName, String optionalInfo, String mediaId);

    int leaveChannel();

    int setChannelProfile(int profile);

    int startEchoTest();

    int stopEchoTest();
    //setMicrophoneEnabled(boolean enable)
    int muteLocalAudioStream(boolean muted);

    int muteAllRemoteAudioStreams(boolean muted);

    int muteRemoteAudioStream(String mediaId, boolean muted);
    //setSpeakerEnabled
    int setEnableSpeakerphone(boolean enabled);

    int startAudioRecording(String filePath);

    int stopAudioRecording();

    String getCallId();

    int rate(String callId, int rating, String description);

    int complain(String callId, String description);

    void monitorHeadsetEvent(boolean monitor);

    void monitorBluetoothHeadsetEvent(boolean monitor);

    void monitorConnectionEvent(boolean monitor);

    boolean isSpeakerphoneEnabled();

    int setSpeakerphoneVolume(int volume);

    int enableAudioVolumeIndication(int interval, int smooth);

    int setVideoProfile(String profile);

    int setVideoBitRate(int minRate, int maxRate);

    int setBeautyEnable(boolean enabled);

    int setLocalRenderMode(int mode);

    int setRemoteRenderMode(String mediaId, int mode);

    void switchView(String mediaId1, String mediaId2);

    int switchCamera();

    int requestNormalUser();

    int requestWhiteBoard();
    //setCameraEnabled(boolean enabled)
    int muteLocalVideoStream(boolean muted);

    int muteAllRemoteVideoStreams(boolean muted);

    int muteRemoteVideoStream(String mediaId, boolean muted);

    int setLogFile(String filePath);

    int setLogFilter(int filter);

    int startServerRecording(String key);

    int stopServerRecording(String key);

    int getServerRecordingStatus();

    void setUserType(int type);

    void answerDegradeNormalUserToObserver(String hostId);

    int answerUpgradeObserverToNormalUser(String userID, boolean isAccept);

    /**
     * 麦克风/摄像头被主持人打开时, 被打开人的应答调用
     *
     * @param userID   用户标识唯一ID
     * @param dType    设备类型
     * @param isOpen   打开/关闭
     * @param isAccept 是否接受
     * @return 0:成功 1:参数错误 2:状态错误
     */
    int answerHostControlUserDevice(String userID, int dType, boolean isOpen, boolean isAccept);
}
