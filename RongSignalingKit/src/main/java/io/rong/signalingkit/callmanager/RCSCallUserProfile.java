package io.rong.signalingkit.callmanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.SurfaceView;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.common.ParcelUtils;

/**
 * Created by weiqinxiao on 16/3/1.
 */
public class RCSCallUserProfile implements Parcelable {
    private String userId;
    private String mediaId;
    private SurfaceView videoView;
    private RCSCallCommon.CallStatus callStatus = RCSCallCommon.CallStatus.IDLE;
    private RCSCallCommon.CallMediaType mediaType = RCSCallCommon.CallMediaType.AUDIO;
    private RCSCallCommon.CallUserType userType = RCSCallCommon.CallUserType.NORMAL;
    private long inviteTime;
    private boolean videoSetup = false;

    public RCSCallUserProfile() {
    }


    /**
     * @param userId     userId
     * @param mediaId    mediaId
     * @param callStatus userCallStatus
     * @param mediaType  mediaType
     */
    public RCSCallUserProfile(String userId, String mediaId,
                              RCSCallCommon.CallStatus callStatus, RCSCallCommon.CallMediaType mediaType) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.callStatus = callStatus;
        this.mediaType = mediaType;
        this.inviteTime = System.currentTimeMillis();
    }

    public RCSCallCommon.CallUserType getUserType() {
        return userType;
    }

    public void setUserType(RCSCallCommon.CallUserType userType) {
        this.userType = userType;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SurfaceView getVideoView() {
        return videoView;
    }

    public void setVideoView(SurfaceView videoView) {
        this.videoView = videoView;
    }

    public boolean getVideoSetup(){
        return this.videoSetup;
    }

    public void setVideoSetup(boolean isSetup){
        this.videoSetup = isSetup;
    }

    public RCSCallCommon.CallStatus getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(RCSCallCommon.CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    public RCSCallCommon.CallMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(RCSCallCommon.CallMediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, userId);
        ParcelUtils.writeToParcel(dest, mediaId);
        ParcelUtils.writeToParcel(dest, mediaType.getValue());
        ParcelUtils.writeToParcel(dest, callStatus.getValue());
        ParcelUtils.writeToParcel(dest, userType.getValue());
    }


    public RCSCallUserProfile(Parcel in) {
        userId = ParcelUtils.readFromParcel(in);
        mediaId = ParcelUtils.readFromParcel(in);
        mediaType = RCSCallCommon.CallMediaType.valueOf(ParcelUtils.readIntFromParcel(in));
        callStatus = RCSCallCommon.CallStatus.valueOf(ParcelUtils.readIntFromParcel(in));
        int callUserType = ParcelUtils.readIntFromParcel(in);
        userType = RCSCallCommon.CallUserType.valueOf(callUserType == 0 ? RCSCallCommon.CallUserType.NORMAL.getValue() : callUserType);
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

    public static final Creator<RCSCallUserProfile> CREATOR = new Creator<RCSCallUserProfile>() {

        @Override
        public RCSCallUserProfile createFromParcel(Parcel source) {
            return new RCSCallUserProfile(source);
        }

        @Override
        public RCSCallUserProfile[] newArray(int size) {
            return new RCSCallUserProfile[size];
        }
    };

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("mediaId", mediaId);
            jsonObject.put("callStatus", callStatus.getValue());
            jsonObject.put("mediaType", mediaType.getValue());
            jsonObject.put("userType", userType.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public RCSCallUserProfile(JSONObject jsonObject) {
        try {
            userId = jsonObject.getString("userId");
            mediaId = jsonObject.optString("mediaId");
            callStatus = RCSCallCommon.CallStatus.valueOf(jsonObject.getInt("callStatus"));
            mediaType = RCSCallCommon.CallMediaType.valueOf(jsonObject.getInt("mediaType"));
            userType = RCSCallCommon.CallUserType.valueOf(jsonObject.getInt("userType"));
            mediaId = jsonObject.getString("mediaId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RCSCallUserProfile{" +
                "userId='" + userId + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", videoView=" + videoView +
                ", callStatus=" + callStatus +
                ", mediaType=" + mediaType +
                ", userType=" + userType +
                '}';
    }

    public long getInviteTime() {
        return inviteTime;
    }
}
