package io.rong.signalingkit;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.signalingkit.callmanager.RCSCallUserProfile;
import io.rong.common.ParcelUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class RCSCallSession implements Parcelable {

    private String callId;
    private Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;
    private String targetId;
    private RCSCallCommon.CallMediaType mediaType = RCSCallCommon.CallMediaType.AUDIO;
    private RCSCallCommon.CallEngineType engineType = RCSCallCommon.CallEngineType.ENGINE_TYPE_RTC;
    private RCSCallCommon.CallUserType userType = RCSCallCommon.CallUserType.NORMAL;
    private long startTime;
    private long activeTime;
    private long endTime;
    private String selfUserId;
    private String inviterUserId;
    private String callerUserId;
    private List<RCSCallUserProfile> usersProfileList;
    private List<String> observerUserList;
    private String extra;

    public RCSCallSession() {

    }

    public RCSCallSession(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            selfUserId = RongIMClient.getInstance().getCurrentUserId();
            callId = jsonObject.getString("callId");
            targetId = jsonObject.optString("targetId");
            conversationType = Conversation.ConversationType.setValue(jsonObject.getInt("conversationType"));
            mediaType = RCSCallCommon.CallMediaType.valueOf(jsonObject.getInt("mediaType"));
            engineType = RCSCallCommon.CallEngineType.valueOf(jsonObject.getInt("engineType"));
            userType = RCSCallCommon.CallUserType.valueOf((jsonObject.getInt("userType")));
            inviterUserId = jsonObject.getString("inviterUserId");
            callerUserId = jsonObject.getString("callerUserId");
            JSONArray jsonArray = jsonObject.getJSONArray("usersProfileList");
            userType = RCSCallCommon.CallUserType.valueOf(jsonObject.getInt("userType"));
            getParticipantProfileList();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                usersProfileList.add(new RCSCallUserProfile(object));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("callId", callId);
            jsonObject.put("targetId", targetId);
            jsonObject.put("conversationType", conversationType.getValue());
            jsonObject.put("mediaType", mediaType.getValue());
            jsonObject.put("engineType", engineType.getValue());
            jsonObject.put("userType", userType.getValue());
            jsonObject.put("inviterUserId", inviterUserId);
            jsonObject.put("callerUserId", callerUserId);
            jsonObject.put("userType", userType.getValue());

            JSONArray jsonArray = new JSONArray();
            for (RCSCallUserProfile profile : usersProfileList) {
                jsonArray.put(profile.getJSONObject());
            }
            jsonObject.put("usersProfileList", jsonArray);
            jsonObject.put("extra", extra);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    /**
     * 当前的观察者列表
     */
    public List<String> getObserverUserList() {
        if (observerUserList == null) {
            observerUserList = new ArrayList<>();
        }
        return observerUserList;
    }

    /**
     * 当前的观察者列表
     */
    public void setObserverUserList(List<String> observerUserList) {
        this.observerUserList = observerUserList;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getSelfUserId() {
        if (TextUtils.isEmpty(selfUserId)) {
            selfUserId = RongIMClient.getInstance().getCurrentUserId();
        }
        return selfUserId;
    }

    /**
     * 通话建立成功之后的本地系统时间戳
     * {@link System#currentTimeMillis()}
     **/
    public long getActiveTime() {
        return activeTime;
    }

    /**
     * 通话建立成功之后的本地系统时间戳
     * {@link System#currentTimeMillis()}
     **/
    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    /**
     * 邀请当前用户加入通话的邀请者
     */
    public String getInviterUserId() {
        return inviterUserId;
    }

    /**
     * 邀请当前用户加入通话的邀请者
     */
    public void setInviterUserId(String inviterUserId) {
        this.inviterUserId = inviterUserId;
    }

    /**
     * 获取发起会话的用户 Id。
     */
    public String getCallerUserId() {
        return callerUserId;
    }

    /**
     * 获取发送消息的用户 Id。
     */
    public void setCallerUserId(String callerUserId) {
        this.callerUserId = callerUserId;
    }

    /**
     * 当前的用户列表,包含观察者列表中的成员
     */
    public List<RCSCallUserProfile> getParticipantProfileList() {
        if (usersProfileList == null)
            usersProfileList = new ArrayList<>();
        return usersProfileList;
    }

    /**
     * 会话远端参与者
     * @return
     */
    public List<RCSCallUserProfile> getRemoteUsers() {
        List<RCSCallUserProfile> remoteUsers = new ArrayList<>();

        if (usersProfileList != null && usersProfileList.size() > 0) {
            for (RCSCallUserProfile userProfile : usersProfileList) {
                if (!TextUtils.equals(RongIMClient.getInstance().getCurrentUserId(), userProfile.getUserId())) {
                    remoteUsers.add(userProfile);
                }
            }
        }
        return remoteUsers;
    }

    public RCSCallUserProfile getCallUserProfile(String callerUserId) {
        if (usersProfileList == null || usersProfileList.isEmpty())
            return null;

        for (RCSCallUserProfile userProfile : usersProfileList) {
            if (TextUtils.equals(callerUserId, userProfile.getUserId()))
                return userProfile;
        }
        return null;
    }

    public List<String> getParticipantUserIds() {
        List<String> userIds = new ArrayList<>();
        for (RCSCallUserProfile profile : usersProfileList) {
            if (!profile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                userIds.add(profile.getUserId());
            }
        }
        return userIds;
    }

    /**
     * 当前的用户列表,包含观察者列表中的成员
     */
    public void addParticipantUser(RCSCallUserProfile user) {
        getParticipantProfileList();

        boolean existed = false;
        for (RCSCallUserProfile userProfile : this.usersProfileList) {
            if (TextUtils.equals(userProfile.getUserId(), user.getUserId())) {
                existed = true;
                if (user.getCallStatus() != null)
                    userProfile.setCallStatus(user.getCallStatus());

                if (user.getMediaType() != null)
                    userProfile.setMediaType(user.getMediaType());
            }
        }
        if (!existed) {
            usersProfileList.add(user);
        }
    }

    public void remoteUser(String userId){
        if (usersProfileList == null && usersProfileList.size() == 0) {
            return;
        }
        Iterator<RCSCallUserProfile> iterator = usersProfileList.iterator();
        while (iterator.hasNext()) {
            RCSCallUserProfile profile = iterator.next();
            if (profile.getUserId().equals(userId)) {
                iterator.remove();
            }
        }
    }

    /**
     * 音视频引擎类型,开发者无需关心
     */
    public RCSCallCommon.CallEngineType getEngineType() {
        return engineType;
    }

    /**
     * 音视频引擎类型,开发者无需关心
     */
    public void setEngineType(RCSCallCommon.CallEngineType engineType) {
        this.engineType = engineType;
    }

    /**
     * 当前用户类型
     */
    public RCSCallCommon.CallUserType getUserType() {
        return userType;
    }

    /**
     * 当前用户类型
     */
    public void setUserType(RCSCallCommon.CallUserType userType) {
        this.userType = userType;
    }

    /**
     * 通话ID
     */
    public String getCallId() {
        return callId;
    }

    /**
     * 通话ID
     */
    public void setCallId(String callId) {
        this.callId = callId;
    }

    public Conversation.ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(Conversation.ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    /**
     * 通话的目标会话ID
     * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     **/
    public String getTargetId() {
        return targetId;
    }

    /**
     * 通话的目标会话ID
     * 根据不同的 conversationType，可能是聊天 Id、讨论组 Id、群组 Id 或聊天室 Id。
     **/
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 音视频通话类型
     */
    public RCSCallCommon.CallMediaType getMediaType() {
        return mediaType;
    }

    /**
     * 音视频通话类型
     */
    public void setMediaType(RCSCallCommon.CallMediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * startTime：记录开始主叫或被叫的本地系统时间戳
     **/
    public long getStartTime() {
        return startTime;
    }

    /**
     * startTime：记录开始主叫或被叫的本地系统时间戳
     **/
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * endTime：记录本次音视频通话挂断(主动\被动) 或 超时 时的本地系统时间戳
     **/
    public long getEndTime() {
        return endTime;
    }

    /**
     * endTime：记录本次音视频通话挂断(主动\被动) 或 超时 时的本地系统时间戳
     * {@link System#currentTimeMillis()}
     **/
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, callId);
        ParcelUtils.writeToParcel(dest, conversationType.getValue());
        ParcelUtils.writeToParcel(dest, targetId);
        ParcelUtils.writeToParcel(dest, mediaType.getValue());
        ParcelUtils.writeToParcel(dest, engineType.getValue());
        ParcelUtils.writeToParcel(dest, userType.getValue());
        ParcelUtils.writeToParcel(dest, startTime);
        ParcelUtils.writeToParcel(dest, activeTime);
        ParcelUtils.writeToParcel(dest, endTime);
        ParcelUtils.writeToParcel(dest, selfUserId);
        ParcelUtils.writeToParcel(dest, inviterUserId);
        ParcelUtils.writeToParcel(dest, callerUserId);
        ParcelUtils.writeListToParcel(dest, usersProfileList);
        ParcelUtils.writeListToParcel(dest, observerUserList);
        ParcelUtils.writeToParcel(dest, extra);
    }


    public RCSCallSession(Parcel in) {
        callId = ParcelUtils.readFromParcel(in);
        conversationType = Conversation.ConversationType.setValue(ParcelUtils.readIntFromParcel(in));
        targetId = ParcelUtils.readFromParcel(in);
        mediaType = RCSCallCommon.CallMediaType.valueOf(ParcelUtils.readIntFromParcel(in));
        engineType = RCSCallCommon.CallEngineType.valueOf(ParcelUtils.readIntFromParcel(in));
        userType = RCSCallCommon.CallUserType.valueOf(ParcelUtils.readIntFromParcel(in));
        startTime = ParcelUtils.readLongFromParcel(in);
        activeTime = ParcelUtils.readLongFromParcel(in);
        endTime = ParcelUtils.readLongFromParcel(in);
        selfUserId = ParcelUtils.readFromParcel(in);
        inviterUserId = ParcelUtils.readFromParcel(in);
        callerUserId = ParcelUtils.readFromParcel(in);
        usersProfileList = ParcelUtils.readListFromParcel(in, RCSCallUserProfile.class);
        observerUserList = ParcelUtils.readListFromParcel(in, String.class);
        extra = ParcelUtils.readFromParcel(in);
    }

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RCSCallSession> CREATOR = new Creator<RCSCallSession>() {

        @Override
        public RCSCallSession createFromParcel(Parcel source) {
            return new RCSCallSession(source);
        }

        @Override
        public RCSCallSession[] newArray(int size) {
            return new RCSCallSession[size];
        }
    };


    @Override
    public String toString() {
        return "RCSCallSession{" +
                "callId='" + callId + '\'' +
                ", conversationType=" + conversationType +
                ", targetId='" + targetId + '\'' +
                ", mediaType=" + mediaType +
                ", engineType=" + engineType +
                ", userType=" + userType +
                ", startTime=" + startTime +
                ", activeTime=" + activeTime +
                ", endTime=" + endTime +
                ", selfUserId='" + selfUserId + '\'' +
                ", inviterUserId='" + inviterUserId + '\'' +
                ", callerUserId='" + callerUserId + '\'' +
                ", usersProfileList=" + getProfileList() +
                ", observerUserList=" + observerUserList +
                ", extra='" + extra + '\'' +
                '}';
    }

    private String getProfileList() {
        if (usersProfileList == null) return "{}";
        StringBuilder stringBuilder = new StringBuilder("{\n");
        for (RCSCallUserProfile callUserProfile : usersProfileList) {
            stringBuilder.append(callUserProfile.toString()).append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /**
     * 获取我的状态
     * @return
     */
    public RCSCallCommon.CallStatus getCallStatus(){
        //todo
         return RCSCallCommon.CallStatus.IDLE;
    }
}
