package io.rong.signalingkit.callmanager;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.signalingkit.RCSCallSession;

public interface IRCSMissedCallListener {
    void onCallMissed(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason);
}