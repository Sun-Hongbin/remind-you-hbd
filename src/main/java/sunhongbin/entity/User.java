package sunhongbin.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * created by SunHongbin on 2020/5/4
 */
@Getter
@Setter
public class User {

    private int uin;

    private String userName;

    private String nickName;

    private String headImgUrl;

    private String sexInEnglins;

    private int sex;

    private String signature;

    public User(JSONObject jsonObject) {
        this.uin = jsonObject.getInteger("Uin");
        this.userName = jsonObject.getString("UserName");
        this.nickName = jsonObject.getString("NickName");
        this.headImgUrl = jsonObject.getString("HeadImgUrl");
        this.signature = jsonObject.getString("Signature");
        this.sex = jsonObject.getInteger("Sex");
        if (1 == sex) {
            this.sexInEnglins = "male";
        } else if (0 == sex) {
            this.sexInEnglins = "female";
        } else {
            this.sexInEnglins = "unknown";
        }
    }

}
