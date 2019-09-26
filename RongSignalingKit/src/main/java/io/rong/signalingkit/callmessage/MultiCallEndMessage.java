package io.rong.signalingkit.callmessage;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.MessageContent;

import static io.rong.signalingkit.RCSCallCommon.CallDisconnectedReason.REMOTE_NETWORK_ERROR;

@MessageTag(value = "RC:MACEMsg", flag = MessageTag.ISPERSISTED)
public class MultiCallEndMessage extends MessageContent{
    private static final String TAG = MultiCallEndMessage.class.getSimpleName();
    private String content;
    private RCSCallCommon.CallDisconnectedReason reason = REMOTE_NETWORK_ERROR;
    private RongIMClient.MediaType mediaType;

    public MultiCallEndMessage() {

    }

    public MultiCallEndMessage(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            this.reason = RCSCallCommon.CallDisconnectedReason.valueOf(jsonObject.getInt("reason"));
            this.mediaType = RongIMClient.MediaType.setValue(jsonObject.getInt("mediaType"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public RongIMClient.MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(RongIMClient.MediaType mediaType) {

        this.mediaType = mediaType;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("reason", reason != null ? reason.getValue() : 3);
            jsonObj.put("mediaType", mediaType != null ? mediaType.getValue() : 1);
        } catch (JSONException e) {
            RLog.e(TAG, "JSONException, " + e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getContent() {
        return content;
    }

    public RCSCallCommon.CallDisconnectedReason getReason() {
        return reason;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReason(RCSCallCommon.CallDisconnectedReason reason) {
        this.reason = reason;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeInt(this.reason == null ? -1 : this.reason.ordinal());
        dest.writeInt(this.mediaType == null ? -1 : this.mediaType.ordinal());
    }

    protected MultiCallEndMessage(Parcel in) {
        this.content = in.readString();
        int tmpReason = in.readInt();
        this.reason = tmpReason == -1 ? null : RCSCallCommon.CallDisconnectedReason.values()[tmpReason];
        int tmpMediaType = in.readInt();
        this.mediaType = tmpMediaType == -1 ? null : RongIMClient.MediaType.values()[tmpMediaType];
    }

    public static final Creator<MultiCallEndMessage> CREATOR = new Creator<MultiCallEndMessage>() {
        @Override
        public MultiCallEndMessage createFromParcel(Parcel source) {
            return new MultiCallEndMessage(source);
        }

        @Override
        public MultiCallEndMessage[] newArray(int size) {
            return new MultiCallEndMessage[size];
        }
    };
}
