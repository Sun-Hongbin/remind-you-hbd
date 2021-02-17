package sunhongbin.enums;

import lombok.Getter;

/**
 * 监听消息时调用API返回的信息
 * created by SunHongbin on 2020/9/14
 */
@Getter
public enum SelectorEnum {

    /**
     * 成功
     */
    NORMAL(0, "成功"),

    /**
     * 有新消息
     */
    NEW_MSG(2, "有新消息"),

    /**
     * 有人修改了自己的昵称或你修改了别人的备注
     */
    MOD_CONTACT(4, "有人修改了自己的昵称或你修改了别人的备注"),

    /**
     * 存在删除或者新增的好友信息
     */
    ADD_OR_DEL_CONTACT(6, "存在删除或者新增的好友信息"),

    /**
     * 手机操作了微信，比如进入或离开聊天界面
     */
    ENTER_OR_LEAVE_CHAT(7, "进入或离开聊天界面");

    private int index;

    private String desc;

    SelectorEnum(int index, String desc) {
        this.index = index;
        this.desc = desc;
    }

    public static SelectorEnum stateOf(int index) {
        for (SelectorEnum selectorEnum : values()) {
            if (selectorEnum.getIndex() == index) {
                return selectorEnum;
            }
        }
        return null;
    }
}
