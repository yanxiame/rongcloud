package cn.sa.im.ui.widget.plugin;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

@SuppressLint("ParcelCreator")
@MessageTag(value = "app:customm", flag = MessageTag.ISCOUNTED|MessageTag.ISPERSISTED)
public class CustomizeMessage extends MessageContent {

    private int id;
    private String userId;
    private String url;

    public CustomizeMessage(int id, String userId, String url) {
        this.id = id;
        this.userId = userId;
        this.url = url;
    }

    public static CustomizeMessage obtain(int id, String userId, String url) {
        return new CustomizeMessage(id, userId, url);
    }
    public static final Creator<CustomizeMessage> CREATOR = new Creator<CustomizeMessage>() {
        @Override
        public CustomizeMessage createFromParcel(Parcel source) {
            return new CustomizeMessage(source);
        }

        @Override
        public CustomizeMessage[] newArray(int size) {
            return new CustomizeMessage[size];
        }
    };


    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId()); // 这里的id（联系人）不同于下边发送名片信息者的 sendUserId
            jsonObject.put("userId", getUserId());
            jsonObject.put("url", getUrl());
        } catch (Exception e) {
        }
        try {
            return jsonObject.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public CustomizeMessage(byte[] data) {
        super(data);
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
            org.json.JSONObject jsonObj = new org.json.JSONObject(jsonStr);
            JSONObject object = JSON.parseObject(jsonStr);
            setId(object.getInteger("userName"));
            setUserId(object.getString("phoneNum"));
            setUrl(object.getString("isReceived"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        ParcelUtils.writeToParcel(dest, id);
        ParcelUtils.writeToParcel(dest, userId);
        ParcelUtils.writeToParcel(dest, url);
    }

    public CustomizeMessage(Parcel parcel) {
        id = ParcelUtils.readIntFromParcel(parcel);
        userId = ParcelUtils.readFromParcel(parcel);
        url = ParcelUtils.readFromParcel(parcel);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}