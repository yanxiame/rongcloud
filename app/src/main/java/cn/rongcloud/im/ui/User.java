package cn.rongcloud.im.ui;

import android.net.Uri;

import java.io.Serializable;

import io.rong.imlib.model.MessageContent;

public class User implements Serializable {

    private String userName;

    private String phone;

    private Uri img;

    private String tagid;

    private MessageContent messageContent;

    private int lastid;

    private char headLetter;

    public User(String userName, String phone, String tagid, int lastid) {
        this.userName = userName;
        this.phone = phone;
        this.tagid=tagid;
        this.lastid=lastid;
        headLetter = Utils.getHeadChar(userName);
    }
    public User(String userName, String phone, String tagid, int lastid, Uri img) {
        this.userName = userName;
        this.phone = phone;
        this.tagid=tagid;
        this.lastid=lastid;
        this.img=img;
        headLetter = Utils.getHeadChar(userName);
    }
    public User(String userName, MessageContent messageContent) {
        this.userName = userName;
        this.messageContent = messageContent;

        headLetter = Utils.getHeadChar(userName);
    }
    public User(String userName, String phone) {
        this.userName = userName;
        this.phone = phone;
        headLetter = Utils.getHeadChar(userName);
    }
    public User(String userName, String phone, Uri uri) {
        this.userName = userName;
        this.phone = phone;
        this.img=uri;
        headLetter = Utils.getHeadChar(userName);
    }

    public int getLastid() {
        return lastid;
    }

    public void setLastid(int lastid) {
        this.lastid = lastid;
    }

    public String getTagid() {
        return tagid;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }

    public Uri getImg() {
        return img;
    }

    public void setImg(Uri img) {
        this.img = img;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    public char getHeadLetter() {
        return headLetter;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        User that = (User) object;
        return getUserName().equals(that.getUserName()) && getPhone().equals(that.getPhone());
    }

    public int compareTo(Object object) {
        if (object instanceof User) {
            User that = (User) object;
            if (getHeadLetter() == ' ') {
                if (that.getHeadLetter() == ' ') {
                    return 0;
                }
                return -1;
            }
            if (that.getHeadLetter() == ' ') {
                return 1;
            } else if (that.getHeadLetter() > getHeadLetter()) {
                return -1;
            } else if (that.getHeadLetter() == getHeadLetter()) {
                return 0;
            }
            return 1;
        } else {
            throw new ClassCastException();
        }
    }

}
