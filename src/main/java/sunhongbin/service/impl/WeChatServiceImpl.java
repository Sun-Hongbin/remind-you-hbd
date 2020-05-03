package sunhongbin.service.impl;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sunhongbin.enums.QrCodeProperties;
import sunhongbin.enums.WeChatApiUrl;
import sunhongbin.service.WeChatService;
import sunhongbin.util.FileUtil;
import sunhongbin.util.QRCodeUtil;
import sunhongbin.util.StringOperationUtil;
import sunhongbin.util.UriRequestUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    /**
     * 存放请求redirect_uri获得的公参
     */
    private Map<String, String> globalParamsMap;

    @Override
    public String getUUID() {

        logger.info("开始获取UUID……");

        Map<String, String> requestParamMap = new HashMap<>();

        // 固定为wx782c26e4c19acffb
        requestParamMap.put("appid", "wx782c26e4c19acffb");
        // 固定为new
        requestParamMap.put("fun", "new");
        // 语言格式
        requestParamMap.put("lang", "zh_CN");

        String result = UriRequestUtil.deGet(WeChatApiUrl.GET_UUID.getUrl(), requestParamMap);

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
    public void showQRCode(String uuid) {

        // download QR code
        String uri = WeChatApiUrl.GET_QR_CODE.getUrl() + "/" + uuid + "?t=webwx";

        File file = UriRequestUtil.doGetFile(uri);

        // using Google open source ZXING tool
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);

        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //默认的容错级别是L,代码中注释是7，设置成M/H的话二维码就越长了
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 二维码四周的边缘大小，值设置的越大，边缘一圈就越厚
        hintMap.put(EncodeHintType.MARGIN, 1);

        try {
            // qrContent: // https://login.weixin.qq.com/l/IYGBnzQjqA==
            String qrContent = QRCodeUtil.translateFileToQrContent(file, hintMap);

            // encode file to bit matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE,
                    QrCodeProperties.QR_CODE_WIDTH.getValue(),
                    QrCodeProperties.QR_CODE_HEIGHT.getValue(), hintMap);

            // translate into qrCode.png
            Path path = new File(FileUtil.getImageFilePath("qrCode.png")).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "png", path);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        logger.info("成功获取二维码并展示给前端");
    }

    @Override
    public String pollForScanRes(String uuid) {

        Map<String, String> paramMap = new HashMap<>();
        // 参数tip : 1表示未扫描 0表示已扫描
        paramMap.put("tip", "1");
        paramMap.put("uuid", uuid);

        /**
         * response：
         * window.code 扫描结果，201表示扫描成功，200表示确认登录，还有一个408，表示超时（一直没有扫码）
         * window.userAvatar用户头像base64编码
         * window.redirect_uri获取初始化信息的重定向Url
         */
        String requestRes = UriRequestUtil.deGet(WeChatApiUrl.IS_SCAN_QR_CODE.getUrl(), paramMap);

        if (StringUtils.isEmpty(requestRes)) {
            logger.error("扫描二维码失败");
            return null;
        }

        // TODO 不以日志形式出现，将信息返回前端
        String code = StringOperationUtil.stringExtract(requestRes, "window.code=(\\d+);");
        if (StringUtils.isEmpty(code)) {

            logger.error("无法从返回报文中获取返回码！");

        } else if (StringUtils.equals(code, "201")) {

            logger.error("扫描成功，请点击确认按钮!");

        } else if (StringUtils.equals(code, "200")) {

            logger.info("扫描成功，正在登陆，请稍候……");

            setGlobalParams(requestRes);

        } else if (StringUtils.equals(code, "408")) {

            logger.error("登陆超时（您一直没有扫码）!");

        } else {

            logger.error("HTTP 返回码 >= 400：" + code);
        }
        return code;
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

    private void setGlobalParams(String requestRes) {

        // 公共参数集合
        this.globalParamsMap = new HashMap<>();

        // res: window.code=200;\nwindow.redirect_uri="https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=Ab6YXYE91BuiEBIfL9YJ8w-k@qrticket_0&uuid=4ZrteSt-zg==&lang=zh_CN&scan=1588342601";
        String redirect_uri = StringOperationUtil.stringExtract(requestRes, "window.redirect_uri=\"(\\S+?)\";") + "&fun=new";

        // 扫描成功则根据返回结果，解析返回的包括redirect_uri并获取一系列的URL，base_uri，webpush_url
        if (StringUtils.isEmpty(redirect_uri)) {
            logger.error("获取重定向 URL 失败");
            return;
        }

        String xml = UriRequestUtil.doGetGlobalParams(redirect_uri);

        if (StringUtils.isEmpty(xml)) {
            globalParamsMap.put("skey", xml.substring(xml.indexOf("<skey>") + "<skey>".length(), xml.indexOf("</skey>")));
            globalParamsMap.put("wxSid", xml.substring(xml.indexOf("<wxSid>") + "<skey>".length(), xml.indexOf("</wxSid>")));
            globalParamsMap.put("wxUin", xml.substring(xml.indexOf("<wxUin>") + "<skey>".length(), xml.indexOf("</wxUin>")));
            globalParamsMap.put("pass_ticket", xml.substring(xml.indexOf("<pass_ticket>") + "<skey>".length(), xml.indexOf("</pass_ticket>")));
        } else {
            throw new IllegalArgumentException("参数解析错误");
        }
    }


}
