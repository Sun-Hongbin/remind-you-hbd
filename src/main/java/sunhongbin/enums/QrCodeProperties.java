package sunhongbin.enums;

/**
 * created by SunHongbin on 2020/5/2
 */
public enum QrCodeProperties {

    /**
     * CODE_WIDTH：二维码宽度，单位像素
     * CODE_HEIGHT：二维码高度，单位像素
     * FRONT_COLOR：二维码前景色，0x000000 表示黑色
     * BACKGROUND_COLOR：二维码背景色，0xFFFFFF 表示白色
     * 演示用 16 进制表示，和前端页面 CSS 的取色是一样的，注意前后景颜色应该对比明显，如常见的黑白
     */
    QR_CODE_WIDTH(200),

    QR_CODE_HEIGHT(200);

    private int value;

    QrCodeProperties(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
