package sunhongbin.enums.error;

import lombok.Getter;

/**
 * created by SunHongbin on 2020/9/11
 */
public enum WeChatInitErrorEnum {

    /**
     * [微信状态提醒] 开启失败
     */
    INIT_ERROR_STATUS_NOTIFY_FAILED("[微信状态提醒] 开启失败");
    
    @Getter
    private String desc;

    WeChatInitErrorEnum(String desc) {
        this.desc = desc;
    }
}
