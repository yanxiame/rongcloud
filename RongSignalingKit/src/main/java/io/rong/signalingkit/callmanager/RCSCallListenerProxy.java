package io.rong.signalingkit.callmanager;

import android.util.Log;
import android.view.SurfaceView;

import java.util.HashSet;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.signalingkit.RCSCallSession;

public class RCSCallListenerProxy {
    private static final String TAG = "RCSCallListenerProxy";

    private HashSet<IRCSCallListener> callListeners = new HashSet<>();

    public HashSet<IRCSCallListener> getCallListeners() {
        return callListeners;
    }

    public static class SingleHolder {
        static RCSCallListenerProxy instance = new RCSCallListenerProxy();
    }

    public static RCSCallListenerProxy getInstance() {
        return RCSCallListenerProxy.SingleHolder.instance;
    }

    public RCSCallListenerProxy() {
    }

    /**
     * 注册通话回调
     * @param callListener
     */
    public void registerCallListener(IRCSCallListener callListener) {
        if (callListener == null)
            return;
        if (!callListeners.contains(callListener))
            callListeners.add(callListener);
    }

    /**
     * 取消注册通话回调
     * @param callListener
     */
    public void unregisterCallListener(IRCSCallListener callListener) {
        if (callListener == null)
            return;
        if (callListeners.contains(callListener))
            callListeners.remove(callListener);
    }

    public void onCallConnected(RCSCallSession callSession, SurfaceView userVideoView) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onCallConnected(callSession, userVideoView);
        }
    }

    public void onCallDisconnected(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onCallDisconnected(callSession, reason);
        }
    }

    public void onRemoteUserInvited(String userId, RCSCallCommon.CallMediaType mediaType) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onRemoteUserInvited(userId, mediaType);
        }
    }

    public void onRemoteUserJoined(String userId, RCSCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onRemoteUserJoined(userId, mediaType, userType, remoteVideo);
        }
    }


    public void onRemoteUserLeft(String userId, RCSCallCommon.CallDisconnectedReason reason) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onRemoteUserLeft(userId, reason);
        }
    }

    public void onCallOutgoing(RCSCallSession callSession, SurfaceView userVideoView) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onCallOutgoing(callSession, userVideoView);
        }
    }

    public void onRemoteUserRinging(String userId) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onRemoteUserRinging(userId);
        }
    }


    public void onFirstRemoteVideoFrame(String mediaId, int width, int height) {
        for (IRCSCallListener callListener : callListeners) {
            Log.e(TAG, "onFirstRemoteVideoFrame: " + callListener);
            callListener.onFirstRemoteVideoFrame(mediaId, width, height);
        }
    }

    public void onMediaTypeChanged(String mediaId, RCSCallCommon.CallMediaType toMediaType, SurfaceView userVideoView) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onMediaTypeChanged(mediaId, toMediaType, userVideoView);
        }
    }

    public void onRemoteCameraDisabled(String userId, boolean disabled) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onRemoteCameraDisabled(userId, disabled);
        }
    }

    public void onError(RCSCallCommon.CallErrorCode errorCode) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onError(errorCode);
        }
    }

    public void onNetworkReceiveLost(String userId, int lossRate) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onNetworkReceiveLost(userId, lossRate);
        }
    }

    public void onNetworkSendLost(int lossRate, int delay) {
        for (IRCSCallListener callListener : callListeners) {
            callListener.onNetworkSendLost(lossRate, delay);
        }
    }
}