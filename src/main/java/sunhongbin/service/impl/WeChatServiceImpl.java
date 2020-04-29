package sunhongbin.service.impl;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sunhongbin.service.WeChatService;
import sunhongbin.util.QRCodeUtil;
import sunhongbin.util.StringOperationUtil;
import sunhongbin.util.UriRequestUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    private final String getUUIDUrl = "https://login.weixin.qq.com/jslogin";

    /**
     * 获取登陆微信二维码方式1
     * 将 String (https://login.weixin.qq.com/l/{$UUID}) 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     *
     * 自己猜测隐藏的弊端：
     * 可能这个URL的格式（login.weixin.qq.com/l/）是一直在变的
     * 优点：
     * 减少一次URL请求
     */
    private final String loginWeChatUrl = "https://login.weixin.qq.com/l/";

    /**
     * 获取登陆微信二维码方式2
     * 先发送请求：https://login.weixin.qq.com/qrcode/{$UUID}
     * 微信返回我们 URL（InputStream），内容为 https://login.weixin.qq.com/l/{$UUID}
     * 将微信返回给我们的 InputStream 转换为 File
     * 将 File 中的内容解析成 String ： https://login.weixin.qq.com/l/{$UUID}
     * 将 String 转换成二维码
     * 扫描二维码，也就是扫描 https://login.weixin.qq.com/l/{$UUID}
     *
     */
    private final String getQrCodeUrl = "https://login.weixin.qq.com/qrcode";


    /**
     * CODE_WIDTH：二维码宽度，单位像素
     * CODE_HEIGHT：二维码高度，单位像素
     * FRONT_COLOR：二维码前景色，0x000000 表示黑色
     * BACKGROUND_COLOR：二维码背景色，0xFFFFFF 表示白色
     * 演示用 16 进制表示，和前端页面 CSS 的取色是一样的，注意前后景颜色应该对比明显，如常见的黑白
     */
    private static final int CODE_WIDTH = 1000;
    private static final int CODE_HEIGHT = 10000;

    @Override
    public String getUUID() {

        Map<String, String> requestParamMap = new HashMap<>();

        requestParamMap.put("appid", "wx782c26e4c19acffb");
        requestParamMap.put("fun", "new");
        requestParamMap.put("lang", "zh_CN");

        String result = UriRequestUtil.deGet(getUUIDUrl, requestParamMap);

        String uuid = "";

        if (!StringUtils.isEmpty(result)) {
            // window.QRLogin.code = 200; window.QRLogin.uuid = "YeNy9w_Sgw==";
            String code = StringOperationUtil.stringExtract(result, "window.QRLogin.code = (\\d+);");

            if (!StringUtils.isEmpty(code) && StringUtils.equals(code, "200")) {
                uuid = StringOperationUtil.stringExtract(result, "window.QRLogin.uuid = \"(.*)\";");
                return uuid;
            } else {
                logger.error("获取UUID码报错，错误码：" + code);
            }
        }
        return uuid;
    }

    @Override
    public String showQRCode(String uuid) {

        // using Google open source ZXING tool
        Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //默认的容错级别是L,代码中注释是7，设置成M/H的话二维码就越长了
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 二维码四周的边缘大小，值设置的越大，边缘一圈就越厚
        hintMap.put(EncodeHintType.MARGIN, 1);

        try {
            // qrContent: // https://login.weixin.qq.com/l/IYGBnzQjqA==
            String qrContent = loginWeChatUrl + uuid;

            // encode file to bit matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT, hintMap);

            // translate into qrCode.png
            Path path = new File("D:/qrCode.png").toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "png", path);

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return "成功获取二维码";
    }

    @Override
    public String showLoginState() {
        return null;
    }

    @Override
    public boolean login() {
        return false;
    }

    @Override
    public void initializeweChat() {

    }

    @Override
    public boolean loadContactPerson() {
        return false;
    }

    @Override
    public void logOutWeChat() {

    }
}
