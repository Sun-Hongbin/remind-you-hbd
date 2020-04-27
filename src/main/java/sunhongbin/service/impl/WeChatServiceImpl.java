package sunhongbin.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sunhongbin.service.WeChatService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class WeChatServiceImpl implements WeChatService {

    private Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);


    @Override
    public String getUUID() {

        logger.info("开始获取UUID……");
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
