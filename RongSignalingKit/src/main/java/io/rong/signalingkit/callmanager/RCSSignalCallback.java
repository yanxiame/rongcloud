package io.rong.signalingkit.callmanager;

import io.rong.signalingkit.RCSCallSession;

public interface RCSSignalCallback {
    void onSuccess(RCSCallSession callSession);

    void onFailed(int errorCode);
}
