package io.rong.signalingkit;


import android.content.Context;
import android.content.Intent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;


public interface RCSCallCustomerHandlerListener {

    List<String> handleActivityResult(int requestCode, int resultCode, Intent data);

    void addMember(Context context, ArrayList<String> currentMemberIds);

    void onRemoteUserInvited(String userId, RCSCallCommon.CallMediaType mediaType);

    void onCallConnected(RCSCallSession callSession, SurfaceView localVideo);

    void onCallDisconnected(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason);

    void onCallMissed(RCSCallSession callSession , RCSCallCommon.CallDisconnectedReason reason);
}
