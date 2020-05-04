package sunhongbin.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * created by SunHongbin on 2020/5/4
 */
public class User {

    private int uin;

    private String userName;

    private String nickName;

    private String headImgUrl;

    private int sex;

    private String signature;

    public User(JSONObject jsonObject) {
        uin = jsonObject.getInteger("Uin");
        userName = jsonObject.getString("UserName");
        nickName = jsonObject.getString("NickName");
        headImgUrl = jsonObject.getString("HeadImgUrl");
        this.sex = jsonObject.getInteger("HeadImgUrl");
        signature = jsonObject.getString("Signature");
    }

    public int getUin() {
        return uin;
    }

    public void setUin(int uin) {
        this.uin = uin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
