package io.rong.signalingkit.callmessage;

import android.os.Parcel;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.signalingkit.RCSCallCommon;
import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

@MessageTag(value = "RC:VSTMsg", flag = MessageTag.ISPERSISTED | MessageTag.ISCOUNTED)
public class CallSTerminateMessage extends MessageContent {
    private final static String TAG = "CallSTerminateMessage";
    private String content;
    private String direction;
    private RCSCallCommon.CallDisconnectedReason reason;
    private RCSCallCommon.CallMediaType mediaType;

    protected String extra;

    /**
     * 获取消息扩展信息
     *
     * @return 扩展信息
     */
    public String getExtra() {
        return extra;
    }

    /**
     * 设置消息扩展信息
     *
     * @param extra 扩展信息
     */
    public void setExtra(String extra) {
        this.extra = extra;
    }

    public RCSCallCommon.CallDisconnectedReason getReason() {
        return reason;
    }

    public void setReason(RCSCallCommon.CallDisconnectedReason reason) {
        this.reason = reason;
    }

    public RCSCallCommon.CallMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(RCSCallCommon.CallMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 将本地消息对象序列化为消息数据。
     *
     * @return 消息数据。
     */
    @Override
    public byte[] encode() {

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("content", content);
            jsonObj.put("reason", reason != null ? reason.getValue() : 3);
            jsonObj.put("mediaType", mediaType != null ? mediaType.getValue() : 1);
            jsonObj.put("direction", direction);

            if (!TextUtils.isEmpty(getExtra()))
                jsonObj.put("extra", getExtra());

            if (getJSONUserInfo() != null)
                jsonObj.putOpt("user", getJSONUserInfo());

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


    public CallSTerminateMessage() {

    }

    public static CallSTerminateMessage obtain(String text) {
        CallSTerminateMessage model = new CallSTerminateMessage();
        model.setContent(text);
        return model;
    }

    public CallSTerminateMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {

        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            content = jsonObj.optString("content");
            direction = jsonObj.optString("direction");
            reason = RCSCallCommon.CallDisconnectedReason.valueOf(jsonObj.optInt("reason"));
            mediaType = RCSCallCommon.CallMediaType.valueOf(jsonObj.optInt("mediaType"));
            if (jsonObj.has("extra"))
                setExtra(jsonObj.optString("extra"));

            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

        } catch (JSONException e) {
            RLog.e(TAG, "JSONException, " + e.getMessage());
        }

    }

    /**
     * 设置文字消息的内容。
     *
     * @param content 文字消息的内容。
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getExtra());
        ParcelUtils.writeToParcel(dest, content);
        ParcelUtils.writeToParcel(dest, getUserInfo());
        ParcelUtils.writeToParcel(dest, reason != null ? reason.getValue() : 3);
        ParcelUtils.writeToParcel(dest, mediaType != null ? mediaType.getValue() : 1);
        ParcelUtils.writeToParcel(dest, direction);
    }

    /**
     * 构造函数。
     *
     * @param in 初始化传入的 Parcel。
     */
    public CallSTerminateMessage(Parcel in) {
        setExtra(ParcelUtils.readFromParcel(in));
        setContent(ParcelUtils.readFromParcel(in));
        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
        reason = RCSCallCommon.CallDisconnectedReason.valueOf(ParcelUtils.readIntFromParcel(in));
        mediaType = RCSCallCommon.CallMediaType.valueOf(ParcelUtils.readIntFromParcel(in));
        direction = ParcelUtils.readFromParcel(in);
    }

    /**
     * 构造函数。
     *
     * @param content 文字消息的内容。
     */
    public CallSTerminateMessage(String content) {
        this.setContent(content);
    }

    /**
     * 获取文字消息的内容。
     *
     * @return 文字消息的内容。
     */
    public String getContent() {
        return content;
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<CallSTerminateMessage> CREATOR = new Creator<CallSTerminateMessage>() {

        @Override
        public CallSTerminateMessage createFromParcel(Parcel source) {
            return new CallSTerminateMessage(source);
        }

        @Override
        public CallSTerminateMessage[] newArray(int size) {
            return new CallSTerminateMessage[size];
        }
    };
}
