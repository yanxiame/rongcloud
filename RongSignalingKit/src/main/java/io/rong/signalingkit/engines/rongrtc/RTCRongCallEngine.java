package io.rong.signalingkit.engines.rongrtc;

import android.content.Context;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.List;

import cn.rongcloud.rtc.CenterManager;
import cn.rongcloud.rtc.RTCErrorCode;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.RongRTCEngine;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.core.CameraVideoCapturer;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.events.ILocalVideoFrameListener;
import cn.rongcloud.rtc.events.RTCVideoFrame;
import cn.rongcloud.rtc.events.RongRTCEventsListener;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.rtc.user.RongRTCLocalUser;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import io.rong.signalingkit.RCSCallCommon;
import io.rong.signalingkit.engines.IRCSCallEngine;
import io.rong.signalingkit.engines.IRCSCallEngineListener;
import io.rong.common.RLog;


import io.rong.imlib.model.Message;

public class RTCRongCallEngine implements IRCSCallEngine {
    private static final String TAG = "RTCRongCallEngine";
    private RongRTCLocalUser localUser;
    private RongRTCRoom rtcRoom;
    private RTCEventHandler rtcEventHandler;
    private Context mContext;
    private HashMap<String, RongRTCVideoView> videoCacheMap;
    private RongRTCVideoView localVideoView;
    private boolean isPreview;


    public RTCRongCallEngine() {
        RLog.i(TAG, "RTCRongCallEngine constructor");
    }

    @Override
    public void create(Context context, IRCSCallEngineListener engineListener) {
        RLog.i(TAG, "create()");
        mContext = context;
        rtcEventHandler = new RTCEventHandler(engineListener);
    }

    @Override
    public void destroy() {
        RLog.i(TAG, "destroy()");
    }

    @Override
    public SurfaceView createRendererView(Context context) {
        RLog.i(TAG, "createRendererView()");
        return RongRTCEngine.getInstance().createVideoView(context);
    }

    @Override
    public void setupLocalVideo(SurfaceView localVideo) {
        RLog.i(TAG, "setupLocalVideo()");
        localVideoView = (RongRTCVideoView) localVideo;
    }

    @Override
    public boolean setupRemoteVideo(SurfaceView remoteVideo, String mediaId) {
        RLog.i(TAG, "setupRemoteVideo()");
        if (videoCacheMap == null) {
            videoCacheMap = new HashMap<String, RongRTCVideoView>();
        }
        RongRTCVideoView videoView = (RongRTCVideoView) remoteVideo;
        videoCacheMap.put(mediaId, videoView);

        // 会话开始后加入的人员，绑定流与SurfaceView
        if (rtcRoom == null)
            return false;
        RongRTCRemoteUser remoteUser = rtcRoom.getRemoteUser(mediaId);
        if (remoteUser == null)
            return false;
        setVideoView(remoteUser);
        return true;
    }

    @Override
    public int enableVideo() {
        RLog.i(TAG, "enableVideo()");
        RongRTCCapture.getInstance().muteLocalVideo(false);
        return 0;
    }

    @Override
    public int disableVideo() {
        RLog.i(TAG, "disableVideo()");
        RongRTCCapture.getInstance().muteLocalVideo(true);
        return 0;
    }

    @Override
    public int startPreview() {
        RLog.i(TAG, "startPreview()");
        isPreview = true;
        RongRTCCapture.getInstance().startCameraCapture();
        return 0;
    }

    @Override
    public int stopPreview() {
        RLog.i(TAG, "stopPreview()");
        isPreview = false;
        RongRTCCapture.getInstance().stopCameraCapture();
        return 0;
    }

    @Override
    public int joinChannel(final String channelName, final String optionalInfo, final String mediaId) {
        RLog.i(TAG, "joinChannel()");
        if (CenterManager.getInstance().getRongRTCRoom() != null) {
            RongRTCEngine.getInstance().quitRoom(CenterManager.getInstance().getRongRTCRoom().getRoomId(), new RongRTCResultUICallBack() {

                @Override
                public void onUiSuccess() {
                    innerJoinChannel(channelName, optionalInfo, mediaId);
                }

                @Override
                public void onUiFailed(RTCErrorCode errorCode) {
                    if (rtcEventHandler != null) {
                        rtcEventHandler.onError(errorCode.getValue());
                    }
                }
            });
        } else {
            innerJoinChannel(channelName, optionalInfo, mediaId);
        }
        return 0;
    }

    private int innerJoinChannel(final String channelName, String optionalInfo, final String mediaId) {
        RLog.i(TAG, "innerJoinChannel " + channelName);
        RongRTCEngine.getInstance().joinRoom(channelName, new JoinRoomUICallBack() {
            @Override
            protected void onUiSuccess(RongRTCRoom rongRTCRoom) {
                RLog.i(TAG, "joinChannel() onUiSuccess()");
                rtcRoom = rongRTCRoom;
                localUser = rongRTCRoom.getLocalUser();
                if (videoCacheMap == null) {
                    videoCacheMap = new HashMap<>();
                }
                rtcRoom.registerEventsListener(new RongRTCAdapterEventListener());
                RongRTCCapture.getInstance().setRongRTCVideoView(localVideoView);
                if (isPreview) {
                    RongRTCCapture.getInstance().startCameraCapture();
                }
                localUser.publishDefaultAVStream(new RongRTCResultUICallBack() {

                    @Override
                    public void onUiSuccess() {
                        RLog.i(TAG, "publishDefaultAVStream onUiSuccess()");
                        if (rtcEventHandler != null) {
                            rtcEventHandler.onJoinChannelSuccess(channelName, mediaId, 0);
                        }
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {
                        RLog.i(TAG, "publishDefaultAVStream onUiFailed()");
                        if (rtcEventHandler != null) {
                            rtcEventHandler.onError(errorCode.getValue());
                        }
                    }
                });

                for (RongRTCRemoteUser remoteUser : rongRTCRoom.getRemoteUsers().values()) {
                    setVideoView(remoteUser);
                    remoteUser.subscribeAvStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
                        @Override
                        public void onUiSuccess() {
                            RLog.i(TAG, "subscribeAvStream onUiSuccess()");
                        }

                        @Override
                        public void onUiFailed(RTCErrorCode errorCode) {
                            RLog.i(TAG, "subscribeAvStream onUiFailed()");
                            if (rtcEventHandler != null) {
                                rtcEventHandler.onError(errorCode.getValue());
                            }
                        }
                    });
                }
            }

            @Override
            protected void onUiFailed(RTCErrorCode errorCode) {
                RLog.i(TAG, "joinChannel() onUiFailed()");
                if (rtcEventHandler != null) {
                    rtcEventHandler.onError(errorCode.getValue());
                }
            }
        });
        return 0;
    }

    /**
     * 关联RongRTCAVInputStream 到 RongRTCVideoView
     *
     * @param remoteUser 远端用户
     */
    private void setVideoView(RongRTCRemoteUser remoteUser) {
        for (RongRTCAVInputStream inputStream : remoteUser.getRemoteAVStreams()) {
            if (inputStream.getMediaType() == MediaType.VIDEO && inputStream.getRongRTCVideoView() == null
                    && videoCacheMap.containsKey(remoteUser.getUserId())) {
                inputStream.setRongRTCVideoView(videoCacheMap.get(remoteUser.getUserId()));
            }
        }
    }

    @Override
    public int leaveChannel() {
        RLog.i(TAG, "leaveChannel()");
        videoCacheMap = null;
        localVideoView = null;
        if (rtcRoom == null)
            return -1;
        RongRTCEngine.getInstance().quitRoom(rtcRoom.getRoomId(), new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                if (rtcEventHandler != null) {
                    rtcEventHandler.onLeaveChannel(rtcRoom.getRoomId());
                }
                RLog.i(TAG, "leaveChannel() onUiSuccess");
                rtcRoom = null;
                localUser = null;
            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {
                RLog.i(TAG, "leaveChannel() onUiFailed");
                if (rtcEventHandler != null) {
                    rtcEventHandler.onError(errorCode.getValue());
                }
            }
        });
        return 0;
    }

    @Override
    public int setChannelProfile(int profile) {
        RLog.i(TAG, "setChannelProfile()");
        return 0;
    }

    @Override
    public int startEchoTest() {
        RLog.i(TAG, "startEchoTest()");
        return 0;
    }

    @Override
    public int stopEchoTest() {
        RLog.i(TAG, "stopEchoTest()");
        return 0;
    }

    @Override
    public int muteLocalAudioStream(boolean muted) {
        RLog.i(TAG, "muteLocalAudioStream() muted = " + muted);
        RongRTCCapture.getInstance().muteMicrophone(muted);
        return 0;
    }

    @Override
    public int muteAllRemoteAudioStreams(boolean muted) {
        RLog.i(TAG, "muteAllRemoteAudioStreams()");
        return 0;
    }

    @Override
    public int muteRemoteAudioStream(String mediaId, boolean muted) {
        RLog.i(TAG, "muteRemoteAudioStream()");
        return 0;
    }

    @Override
    public int setEnableSpeakerphone(boolean enabled) {
        RLog.i(TAG, "setEnableSpeakerphone()");
        RongRTCCapture.getInstance().setEnableSpeakerphone(enabled);
        return 0;
    }

    @Override
    public int startAudioRecording(String filePath) {
        RLog.i(TAG, "startAudioRecording()");
        return 0;
    }

    @Override
    public int stopAudioRecording() {
        RLog.i(TAG, "stopAudioRecording()");
        return 0;
    }

    @Override
    public String getCallId() {
        RLog.i(TAG, "getCallId()");
        if (rtcRoom != null) {
            return rtcRoom.getRoomId();
        }
        return null;
    }

    @Override
    public int rate(String callId, int rating, String description) {
        RLog.i(TAG, "rate()");
        return 0;
    }

    @Override
    public int complain(String callId, String description) {
        RLog.i(TAG, "complain()");
        return 0;
    }

    @Override
    public void monitorHeadsetEvent(boolean monitor) {
        RLog.i(TAG, "monitorHeadsetEvent()");
    }

    @Override
    public void monitorBluetoothHeadsetEvent(boolean monitor) {
        RLog.i(TAG, "monitorBluetoothHeadsetEvent()");
    }

    @Override
    public void monitorConnectionEvent(boolean monitor) {
        RLog.i(TAG, "monitorConnectionEvent()");
    }

    @Override
    public boolean isSpeakerphoneEnabled() {
        RLog.i(TAG, "isSpeakerphoneEnabled()");
        return false;
    }

    @Override
    public int setSpeakerphoneVolume(int volume) {
        RLog.i(TAG, "setSpeakerphoneVolume()");
        RongRTCCapture.getInstance().setEnableSpeakerphone(volume > 0);
        return 0;
    }

    @Override
    public int enableAudioVolumeIndication(int interval, int smooth) {
        RLog.i(TAG, "enableAudioVolumeIndication()");
        return 0;
    }

    @Override
    public int setVideoProfile(String profile) {
        RongRTCConfig.RongRTCVideoProfile videoProfile = RongRTCConfig.RongRTCVideoProfile.getRongRTCVideoProfile(profile);
        RLog.i(TAG, "setVideoProfile()" + profile + " VideoProfile " + videoProfile);
        rtcRoom.changeVideoSize(videoProfile);
        return 0;
    }

    @Override
    public int setVideoBitRate(int minRate, int maxRate) {
//        RongRTCConfig rtcConfig = CenterManager.getInstance().getRTCConfig();
//        RongRTCConfig.Builder builder = new RongRTCConfig.Builder();
//        builder.setMinRate(minRate);
//        builder.setMaxRate(maxRate);
//        builder.build();
//
//        RongRTCCapture.getInstance().setRTCConfig(rtcConfig);

        return 0;
    }

    @Override
    public int setBeautyEnable(boolean enabled) {
        RongRTCCapture.getInstance().setLocalVideoFrameListener(true, new ILocalVideoFrameListener() {
            @Override
            public RTCVideoFrame processVideoFrame(RTCVideoFrame rtcVideoFrame) {
                return null;
            }
        });

        return 0;
    }

    @Override
    public int setLocalRenderMode(int mode) {
        RLog.i(TAG, "setLocalRenderMode()");
        return 0;
    }

    @Override
    public int setRemoteRenderMode(String mediaId, int mode) {
        RLog.i(TAG, "setRemoteRenderMode()");
        return 0;
    }

    @Override
    public void switchView(String mediaId1, String mediaId2) {
        RLog.i(TAG, "switchView()");
    }

    @Override
    public int switchCamera() {
        RLog.i(TAG, "switchCamera()");
        RongRTCCapture.getInstance().switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean isFrontCamera) {
            }

            @Override
            public void onCameraSwitchError(String errorDescription) {
            }
        });
        return 0;
    }

    @Override
    public int requestNormalUser() {
        RLog.i(TAG, "requestNormalUser()");
        return 0;
    }

    @Override
    public int requestWhiteBoard() {
        RLog.i(TAG, "requestWhiteBoard()");

        return 0;
    }

    @Override
    public int muteLocalVideoStream(boolean muted) {
        RLog.i(TAG, "muteLocalVideoStream()");
        RongRTCCapture.getInstance().muteLocalVideo(muted);
        return 0;
    }

    @Override
    public int muteAllRemoteVideoStreams(boolean muted) {
        RLog.i(TAG, "muteAllRemoteVideoStreams()");
        return 0;
    }

    @Override
    public int muteRemoteVideoStream(String mediaId, boolean muted) {
        return 0;
    }

    @Override
    public int setLogFile(String filePath) {
        return 0;
    }

    @Override
    public int setLogFilter(int filter) {
        return 0;
    }

    @Override
    public int startServerRecording(String key) {
        return 0;
    }

    @Override
    public int stopServerRecording(String key) {
        return 0;
    }

    @Override
    public int getServerRecordingStatus() {
        return 0;
    }

    @Override
    public void setUserType(int type) {

    }

    @Override
    public void answerDegradeNormalUserToObserver(String hostId) {

    }

    @Override
    public int answerUpgradeObserverToNormalUser(String userID, boolean isAccept) {
        return 0;
    }

    @Override
    public int answerHostControlUserDevice(String userID, int dType, boolean isOpen, boolean isAccept) {
        return 0;
    }

    private class RongRTCAdapterEventListener implements RongRTCEventsListener {

        @Override
        public void onRemoteUserPublishResource(final RongRTCRemoteUser remoteUser, List<RongRTCAVInputStream> publishResource) {
            setVideoView(remoteUser);
            remoteUser.subscribeAvStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
                @Override
                public void onUiSuccess() {
                    RLog.i(TAG, "onRemoteUserPublishResource remoteUser " + remoteUser.getUserId() + " subscribeAvStream success");
                }

                @Override
                public void onUiFailed(RTCErrorCode errorCode) {
                    RLog.i(TAG, "onRemoteUserPublishResource remoteUser " + remoteUser.getUserId() + " subscribeAvStream failed");
                }
            });
        }

        @Override
        public void onRemoteUserAudioStreamMute(RongRTCRemoteUser remoteUser, RongRTCAVInputStream avInputStream, boolean mute) {

        }

        @Override
        public void onRemoteUserVideoStreamEnabled(RongRTCRemoteUser remoteUser, RongRTCAVInputStream avInputStream, boolean enable) {
            if (rtcEventHandler != null) {
                rtcEventHandler.onUserMuteVideo(remoteUser.getUserId(), !enable);
            }
        }

        @Override
        public void onRemoteUserUnpublishResource(RongRTCRemoteUser remoteUser, List<RongRTCAVInputStream> unPublishResource) {

        }

        @Override
        public void onUserJoined(final RongRTCRemoteUser remoteUser) {
            RLog.i(TAG, "onUserJoined " + remoteUser.getUserId());
            if (rtcEventHandler != null) {
                rtcEventHandler.onUserJoined(remoteUser.getUserId(), 1);
            }
        }

        @Override
        public void onUserLeft(RongRTCRemoteUser remoteUser) {
            if (videoCacheMap == null)
                return;
            videoCacheMap.remove(remoteUser.getUserId());
        }

        @Override
        public void onUserOffline(RongRTCRemoteUser remoteUser) {
            if (rtcEventHandler != null) {
                rtcEventHandler.onUserOffline(remoteUser.getUserId(), 17);
            }
        }

        @Override
        public void onVideoTrackAdd(String userId, String tag) {

        }

        @Override
        public void onFirstFrameDraw(String userId, String tag) {
            if (rtcEventHandler != null) {
                rtcEventHandler.onFirstRemoteVideoFrame(userId, 0, 0, 0);
            }
        }

        @Override
        public void onLeaveRoom() {
            if (rtcEventHandler != null) {
                rtcEventHandler.onLeaveChannel((rtcRoom != null ? rtcRoom.getRoomId() : null));
            }
        }

        @Override
        public void onReceiveMessage(Message message) {

        }
    }
}
