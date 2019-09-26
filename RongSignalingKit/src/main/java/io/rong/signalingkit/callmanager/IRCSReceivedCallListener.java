package io.rong.signalingkit.callmanager;

import io.rong.signalingkit.RCSCallSession;
import io.rong.signalingkit.RCSCallCommon;

public interface IRCSReceivedCallListener {
    /**
     * 来电回调
     *
     * @param callSession 通话实体
     */
    void onReceivedCall(RCSCallSession callSession);
}
