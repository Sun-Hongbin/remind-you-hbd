package sunhongbin.enums;

import lombok.Getter;

/**
 * 监听消息时调用API返回的信息
 * created by SunHongbin on 2020/9/14
 */
@Getter
public enum SyncChecSelectorEnum {

    NORMAL(0, "成功"),

    NEW_MSG(2, "有新消息"),

    MOD_CONTACT(4, "有人修改了自己的昵称或你修改了别人的备注"),

    ADD_OR_DEL_CONTACT(6, "存在删除或者新增的好友信息"),

    ENTER_OR_LEAVE_CHAT(7, "进入或离开聊天界面");

    private int index;

    private String desc;

    SyncChecSelectorEnum(int index, String desc) {
        this.index = index;
        this.desc = desc;
    }

    public static SyncChecSelectorEnum stateOf(int index) {
        for (SyncChecSelectorEnum selectorEnum : values()) {
            if (selectorEnum.getIndex() == index) {
                return selectorEnum;
            }
        }
        return null;
    }
}
