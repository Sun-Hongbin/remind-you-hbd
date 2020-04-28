package sunhongbin.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sunhongbin.service.WeChatService;
import sunhongbin.util.StringOperationUtil;
import sunhongbin.util.UriRequestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    private final String getUUIDUrl = "https://login.weixin.qq.com/jslogin";

    private String uuid;

    @Override
    public String getUUID() {

        Map<String, String> requestParamMap = new HashMap<>();

        requestParamMap.put("appid", "wx782c26e4c19acffb");
        requestParamMap.put("fun", "new");
        requestParamMap.put("lang", "zh_CN");

        String result = UriRequestUtil.deGet(getUUIDUrl, requestParamMap);

        if (!StringUtils.isEmpty(result)) {
            // window.QRLogin.code = 200; window.QRLogin.uuid = "YeNy9w_Sgw==";
            String code = StringOperationUtil.stringExtract(result, "window.QRLogin.code = (\\d+);");

            if (!StringUtils.isEmpty(code) && StringUtils.equals(code, "200")) {
                this.uuid = StringOperationUtil.stringExtract(result, "window.QRLogin.uuid = \"(.*)\";");
                return this.uuid;
            } else {
                logger.error("获取UUID码报错，错误码：" + code);
            }
        }
        return null;
    }

    @Override
    public void showQRCode() {

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
