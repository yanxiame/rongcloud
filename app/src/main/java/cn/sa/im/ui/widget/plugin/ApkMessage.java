package cn.sa.im.ui.widget.plugin;

import android.os.Parcel;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

@MessageTag(value = "app:APKMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class ApkMessage extends MessageContent {

    private String userName;
    private String phoneNum;
    protected String extra;



    private Integer isReceived;


    @Override
    public byte[] encode() {
        JSONObject object =new JSONObject();
        if (!TextUtils.isEmpty(this.getExtra())) {
            object.put("extra", this.getExtra());
        }

        object.put("userName",userName);
        object.put("phoneNum",phoneNum);
        object.put("isReceived",isReceived);
        try {
            return object.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }



    public ApkMessage(byte[] data) {
        super(data);
        String jsonStr=null;
        try {
            jsonStr =new String(data,"UTF-8");
            org.json.JSONObject jsonObj = new org.json.JSONObject(jsonStr);
            JSONObject object = JSON.parseObject(jsonStr);
            if (jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }
            setUserName(object.getString("userName"));
            setPhoneNum(object.getString("phoneNum"));
            setPhoneNum(object.getString("isReceived"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, userName);
        ParcelUtils.writeToParcel(dest,phoneNum);
        ParcelUtils.writeToParcel(dest,isReceived);
        ParcelUtils.writeToParcel(dest, this.getExtra());
    }

    public static final Creator<ApkMessage> CREATOR = new Creator<ApkMessage>() {
        @Override
        public ApkMessage createFromParcel(Parcel source) {
            return new ApkMessage(source);
        }
        @Override
        public ApkMessage[] newArray(int size) {
            return new ApkMessage[size];
        }
    };
    public ApkMessage(){

    }

    public ApkMessage(Parcel parcel){
        userName = ParcelUtils.readFromParcel(parcel);
        phoneNum = ParcelUtils.readFromParcel(parcel);
        isReceived = ParcelUtils.readIntFromParcel(parcel);
        this.setExtra(ParcelUtils.readFromParcel(parcel));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
    public Integer getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(Integer isReceived) {
        this.isReceived = isReceived;
    }
}