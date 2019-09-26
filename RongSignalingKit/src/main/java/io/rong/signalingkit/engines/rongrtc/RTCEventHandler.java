package io.rong.signalingkit.engines.rongrtc;


import io.rong.signalingkit.engines.IRCSCallEngineListener;

/**
 * 可以针对引擎做一些适配操作
 */
public class RTCEventHandler implements IRCSCallEngineListener {
    private IRCSCallEngineListener callEngineListener;

    public RTCEventHandler(IRCSCallEngineListener callEngineListener) {
        this.callEngineListener = callEngineListener;
    }

    @Override
    public void onJoinChannelSuccess(String channel, String mediaId, int elapsed) {
        if (callEngineListener != null) {
            callEngineListener.onJoinChannelSuccess(channel, mediaId, elapsed);
        }
    }

    @Override
    public void onRejoinChannelSuccess(String channel, String mediaId, int elapsed) {
        if (callEngineListener != null) {
            callEngineListener.onRejoinChannelSuccess(channel, mediaId, elapsed);
        }
    }

    @Override
    public void onWarning(int warn) {
        if (callEngineListener != null) {
            callEngineListener.onWarning(warn);
        }

    }

    @Override
    public void onError(int err) {
        if (callEngineListener != null) {
            callEngineListener.onError(err);
        }
    }

    @Override
    public void onApiCallExecuted(String api, int error) {
        if (callEngineListener != null) {
            callEngineListener.onApiCallExecuted(api, error);
        }
    }

    @Override
    public void onCameraReady() {
        if (callEngineListener != null) {
            callEngineListener.onCameraReady();
        }

    }

    @Override
    public void onVideoStopped() {
        if (callEngineListener != null) {
            callEngineListener.onVideoStopped();
        }
    }

    @Override
    public void onAudioQuality(String mediaId, int quality, short delay, short lost) {
        if (callEngineListener != null) {
            callEngineListener.onAudioQuality(mediaId, quality, delay, lost);
        }
    }

    @Override
    public void onLeaveChannel(String channel) {
        if (callEngineListener != null) {
            callEngineListener.onLeaveChannel(channel);
        }

    }

    @Override
    public void onRtcStats() {
        if (callEngineListener != null) {
            callEngineListener.onRtcStats();
        }

    }

    @Override
    public void onAudioVolumeIndication(int totalVolume) {
        if (callEngineListener != null) {
            callEngineListener.onAudioVolumeIndication(totalVolume);
        }

    }

    @Override
    public void onUserJoined(String mediaId, int userType) {
        if (callEngineListener != null) {
            callEngineListener.onUserJoined(mediaId, userType);
        }
    }

    @Override
    public void onUserOffline(String mediaId, int reason) {
        if (callEngineListener != null) {
            callEngineListener.onUserOffline(mediaId, reason);
        }
    }

    @Override
    public void onUserMuteAudio(String mediaId, boolean muted) {
        if (callEngineListener != null) {
            callEngineListener.onUserMuteAudio(mediaId, muted);
        }
    }

    @Override
    public void onUserMuteVideo(String mediaId, boolean muted) {
        if (callEngineListener != null) {
            callEngineListener.onUserMuteVideo(mediaId, muted);
        }
    }

    @Override
    public void onRemoteVideoStat(String mediaId, int delay, int receivedBitrate, int receivedFrameRate) {
        if (callEngineListener != null) {
            callEngineListener.onRemoteVideoStat(mediaId, delay, receivedBitrate, receivedFrameRate);
        }
    }

    @Override
    public void onLocalVideoStat(int sentBitrate, int sentFrameRate) {
        if (callEngineListener != null) {
            callEngineListener.onLocalVideoStat(sentBitrate, sentFrameRate);
        }
    }

    @Override
    public void onFirstRemoteVideoFrame(String mediaId, int width, int height, int elapsed) {
        if (callEngineListener != null) {
            callEngineListener.onFirstRemoteVideoFrame(mediaId, width, height, elapsed);
        }
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        if (callEngineListener != null) {
            callEngineListener.onFirstLocalVideoFrame(width, height, elapsed);
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(String mediaId, int width, int height, int elapsed) {
        if (callEngineListener != null) {
            callEngineListener.onFirstRemoteVideoFrame(mediaId, width, height, elapsed);
        }
    }

    @Override
    public void onConnectionLost() {
        if (callEngineListener != null) {
            callEngineListener.onConnectionLost();
        }
    }

    @Override
    public void onConnectionInterrupted() {
        if (callEngineListener != null) {
            callEngineListener.onConnectionInterrupted();
        }
    }

    @Override
    public void onMediaEngineEvent(int code) {
        if (callEngineListener != null) {
            callEngineListener.onMediaEngineEvent(code);
        }

    }

    @Override
    public void onVendorMessage(String mediaId, byte[] data) {
        if (callEngineListener != null) {
            callEngineListener.onVendorMessage(mediaId, data);
        }
    }

    @Override
    public void onRefreshRecordingServiceStatus(int status) {
        if (callEngineListener != null) {
            callEngineListener.onRefreshRecordingServiceStatus(status);
        }
    }

    @Override
    public void onWhiteBoardURL(String url) {
        if (callEngineListener != null) {
            callEngineListener.onWhiteBoardURL(url);
        }
    }

    @Override
    public void onNetworkReceiveLost(int lossRate) {
        if (callEngineListener != null) {
            callEngineListener.onNetworkReceiveLost(lossRate);
        }
    }

    @Override
    public void onNotifySharingScreen(boolean isSharing) {
        if (callEngineListener != null) {
            callEngineListener.onNotifySharingScreen(isSharing);
        }
    }

    @Override
    public void onNotifyDegradeNormalUserToObserver(String hostUid, String userId) {
        if (callEngineListener != null) {
            callEngineListener.onNotifyDegradeNormalUserToObserver(hostUid, userId);
        }
    }

    @Override
    public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {
        if (callEngineListener != null) {
            callEngineListener.onNotifyAnswerObserverRequestBecomeNormalUser(userId, status);
        }
    }

    @Override
    public void onNotifyUpgradeObserverToNormalUser(String hostUid, String userId) {
        if (callEngineListener != null) {
            callEngineListener.onNotifyUpgradeObserverToNormalUser(hostUid, userId);
        }
    }

    @Override
    public void onNotifyHostControlUserDevice(String userId, String hostId, int dType, boolean isOpen) {
        if (callEngineListener != null) {
            callEngineListener.onNotifyHostControlUserDevice(userId, hostId, dType, isOpen);
        }
    }
}
