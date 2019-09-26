package io.rong.signalingkit;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.rong.signalingkit.callmanager.IRCSMissedCallListener;
import io.rong.signalingkit.callmanager.IRCSReceivedCallListener;
import io.rong.signalingkit.callmanager.RCSCallEngineConfig;
import io.rong.signalingkit.callmanager.RCSCallListenerProxy;
import io.rong.signalingkit.callmanager.RCSCallManager;
import io.rong.signalingkit.callmessage.CallEndMessageItemProvider;
import io.rong.signalingkit.callmessage.CallSTerminateMessage;
import io.rong.signalingkit.callmessage.MultiCallEndMessage;
import io.rong.signalingkit.callmessage.MultiCallEndMessageProvider;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IExternalModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.signalingkit.engines.IRCSCallEngine;
import io.rong.signalingkit.engines.rongrtc.RTCRongCallEngine;

/**
 * Created by weiqinxiao on 16/8/15.
 */
public class RCSCallModule implements IExternalModule {
    private final static String TAG = "RCSCallModule";

    private boolean mViewLoaded = false;
    private Context mContext;
    private RCSCallSession mCallSession;

    public RCSCallModule() {
        RLog.i(TAG, "Constructor");
    }

    @Override
    public void onInitialized(String appKey) {
        try {
            RongIMClient.registerMessageType(MultiCallEndMessage.class);
            RongIMClient.registerMessageType(CallSTerminateMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }

        RongIM.registerMessageTemplate(new CallEndMessageItemProvider());
        RongIM.registerMessageTemplate(new MultiCallEndMessageProvider());
        initMissedCallListener();

        IRCSCallEngine callEngine = null;
        if (RCSCallEngineConfig.EnginePreferred == RCSCallCommon.CallEngineType.ENGINE_TYPE_RTC) {
            callEngine = new RTCRongCallEngine();
        } else if (RCSCallEngineConfig.EnginePreferred == RCSCallCommon.CallEngineType.ENGINE_TYPE_AGORA) {
        }
        RLog.i(TAG, "RCSCallEngineConfig.EnginePreferred = " + RCSCallEngineConfig.EnginePreferred);
        RCSCallClient.getInstance().setEngine(callEngine);
    }

    private void initMissedCallListener() {
        RCSCallManager.getInstance().setMissedCallListener(new IRCSMissedCallListener() {
            @Override
            public void onCallMissed(RCSCallSession callSession, RCSCallCommon.CallDisconnectedReason reason) {
                if (!TextUtils.isEmpty(callSession.getInviterUserId())) {
                    if (callSession.getConversationType() == Conversation.ConversationType.PRIVATE) {
                        CallSTerminateMessage message = new CallSTerminateMessage();
                        message.setReason(reason);
                        message.setMediaType(callSession.getMediaType());
                        String extra;
                        long time = (callSession.getEndTime() - callSession.getStartTime()) / 1000;
                        if (time >= 3600) {
                            extra = String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60));
                        } else {
                            extra = String.format("%02d:%02d", (time % 3600) / 60, (time % 60));
                        }
                        message.setExtra(extra);

                        String senderId = callSession.getInviterUserId();
                        if (senderId.equals(callSession.getSelfUserId())) {
                            message.setDirection("MO");
                            Log.e(TAG, "onCallMissed: targetId " + callSession.getTargetId() + " callerId " + callSession.getCallerUserId());
                            RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), io.rong.imlib.model.Message.SentStatus.SENT, message, callSession.getStartTime(), null);
                        } else {
                            message.setDirection("MT");
                            io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                            receivedStatus.setRead();
                            Log.e(TAG, "onCallMissed: targetId " + callSession.getTargetId() + " callerId " + callSession.getCallerUserId());
                            RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), senderId, receivedStatus, message, callSession.getStartTime(), null);
                        }
                    } else if (callSession.getConversationType() == Conversation.ConversationType.GROUP) {
                        MultiCallEndMessage multiCallEndMessage = new MultiCallEndMessage();
                        multiCallEndMessage.setReason(reason);
                        if (callSession.getMediaType() == RCSCallCommon.CallMediaType.AUDIO) {
                            multiCallEndMessage.setMediaType(RongIMClient.MediaType.AUDIO);
                        } else if (callSession.getMediaType() == RCSCallCommon.CallMediaType.VIDEO) {
                            multiCallEndMessage.setMediaType(RongIMClient.MediaType.VIDEO);
                        }
                        Message.ReceivedStatus receivedStatus = new Message.ReceivedStatus(0);
                        RongIM.getInstance().insertIncomingMessage(callSession.getConversationType(), callSession.getTargetId(), callSession.getCallerUserId(), receivedStatus, multiCallEndMessage, callSession.getStartTime(), null);
                    }
                }
            }
        });
    }


    @Override
    public void onConnected(String token) {
        /**
         * 是否纹理采集,默认是
         * * @param isTexture 设置视频采集方式 :
         * true:texture方式采集，该采集模式下回调方法返回对象{@link io.rong.rongcall.CallVideoFrame}中视频数据体现在{@link io.rong.rongcall.CallVideoFrame#oesTextureId},而{@link io.rong.rongcall.CallVideoFrame#data}byte数据为空;
         * false:yuv方式采集，该采集模式下回调方法返回对象{@link io.rong.rongcall.CallVideoFrame}中视频数据体现在{@link io.rong.rongcall.CallVideoFrame#data},而{@link io.rong.rongcall.CallVideoFrame#oesTextureId}oesTextureId为0;
         */
//        RCSCallClient.getInstance().setCaptureType(true);
//        RCSCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
//        // 开启音视频日志，如果不需要开启，则去掉下面这句。
//        RCSCallClient.getInstance().setEnablePrintLog(true);
//        RCSCallClient.getInstance().setVideoProfile(RCSCallCommon.CallVideoProfile.VD_480x640_15f);
        RLog.i(TAG, "onConnected()");
    }

    @Override
    public void onCreate(final Context context) {
        Log.i(TAG, "onCreate()");
        mContext = context;
        RCSCallClient.getInstance().init(context);
        IRCSReceivedCallListener callListener = new IRCSReceivedCallListener() {
            @Override
            public void onReceivedCall(final RCSCallSession callSession) {
                RLog.d(TAG, "onReceivedCall");
                if (mViewLoaded) {
                    RLog.d(TAG, "onReceivedCall->onCreate->mViewLoaded=true");
                    RCSCallClient.getInstance().startVoIPActivity(mContext, callSession, false);
                } else {
                    RCSCallModule.this.mCallSession = callSession;
                    RLog.d(TAG, "onReceivedCall->onCreate->mViewLoaded=false mCallSession = " + mCallSession);
                }
            }
        };
        RCSCallClient.getInstance().setReceivedCallListener(callListener);
    }

    /**
     * 此方法的目的是，防止 voip 通话界面被会话或者会话列表界面覆盖。
     * 所有要等待会话或者会话列表加载出后，再显示voip 通话界面。
     * <p>
     * 当会话列表或者会话界面加载出来后，此方法会被回调。
     * 如果开发者没有会话或者会话列表界面，只需要将下面的 mViewLoaded 在 onCreate 时设置为 true 即可。
     */
    @Override
    public void onViewCreated() {
        RLog.i(TAG, "onViewCreated: ");
        mViewLoaded = true;
        if (RCSCallModule.this.mCallSession != null) {
            RCSCallClient.getInstance().startVoIPActivity(mContext, RCSCallModule.this.mCallSession, false);
            RLog.e(TAG, "onViewCreated: showedSession " + RCSCallModule.this.mCallSession);
        }
    }

    @Override
    public List<IPluginModule> getPlugins(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = new ArrayList<>();
        try {
            if (true) {
                pluginModules.add(new AudioPlugin());
                pluginModules.add(new VideoPlugin());
            }
        } catch (Exception e) {
            e.printStackTrace();
            RLog.i(TAG, "getPlugins()->Error :" + e.getMessage());
        }
        return pluginModules;
    }

    @Override
    public void onDisconnected() {
        RLog.i(TAG, "onDisconnected()");
        mViewLoaded = false;
    }


}
