package cn.sa.im.ui.widget.plugin;

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

@MessageTag(value = "app:InsertMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class InsertMessage extends MessageContent {

    private Map<Integer,String> mapmessage;
    protected String extra;

    @Override
    public byte[] encode() {
        JSONObject object =new JSONObject();
        if (!TextUtils.isEmpty(this.getExtra())) {
            object.put("extra", this.getExtra());
        }

        object.put("Mapmessage",mapmessage);
        try {
            return object.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }



    public InsertMessage(byte[] data) {
        super(data);
        String jsonStr=null;
        try {
            jsonStr =new String(data,"UTF-8");
            org.json.JSONObject jsonObj = new org.json.JSONObject(jsonStr);
            JSONObject object = JSON.parseObject(jsonStr);
            if (jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }
            //setMapmessage(JSONObject.toJavaObject(object, Map.class));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, mapmessage);
        ParcelUtils.writeToParcel(dest, this.getExtra());
    }

    public static final Creator<InsertMessage> CREATOR = new Creator<InsertMessage>() {
        @Override
        public InsertMessage createFromParcel(Parcel source) {
            return new InsertMessage(source);
        }
        @Override
        public InsertMessage[] newArray(int size) {
            return new InsertMessage[size];
        }
    };
    public InsertMessage(){

    }

    public InsertMessage(Parcel parcel){
        mapmessage = ParcelUtils.readMapFromParcel(parcel);

        this.setExtra(ParcelUtils.readFromParcel(parcel));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Map<Integer, String> Mapmessage() {
        return mapmessage;
    }

    public void setMapmessage(Map mapmessage) {
        this.mapmessage = mapmessage;
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}