package sunhongbin.enums;

import lombok.Getter;

/**
 * 监听消息时调用API返回的信息
 * created by SunHongbin on 2020/9/14
 */
@Getter
public enum RetCodeEnum {

    SUCCESS(0, "成功"),

    TICKET_ERROR(-14, "ticket错误"),

    PARAM_ERROR(1, "传入参数错误"),

    NOT_LOGIN_WARN(1100, "未登录提示"),

    NOT_LOGIN_CHECK(1101, "未检测到登录"),

    COOKIE_INVALID_ERROR(1102, "cookie值无效"),

    LOGIN_ENV_ERROR(1203, "当前登录环境异常，为了安全起见请不要在web端进行登录"),

    TOO_OFTEN(1205, "操作频繁");

    private final int index;

    private final String desc;

    RetCodeEnum(int index, String desc) {
        this.index = index;
        this.desc = desc;
    }

    public static RetCodeEnum stateOf(int index) {
        for (RetCodeEnum retCodeEnum : values()) {
            if (retCodeEnum.getIndex() == index) {
                return retCodeEnum;
            }
        }
        return null;
    }
}
