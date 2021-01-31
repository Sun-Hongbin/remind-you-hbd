package sunhongbin.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sunhongbin.exception.WeChatException;
import sunhongbin.service.LoginAndOutService;
import sunhongbin.service.WeChatService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by SunHongbin on 2020/9/10
 */
@Service
public class LoginAndOutServiceImpl implements LoginAndOutService {

    @Autowired
    private WeChatService weChatService;

    private static final Logger LOG = LoggerFactory.getLogger(LoginAndOutServiceImpl.class);

    /**
     * 第一次请求登陆微信时，就将该值锁住，直到有注销微信的指令到来
     */
    private AtomicBoolean locked = new AtomicBoolean(false);

    @Override
    public String doLogin() throws WeChatException {

        String uuid = weChatService.getUUID();

        // release lock and remind the user to login again
        if (StringUtils.isEmpty(uuid)) {
            locked = new AtomicBoolean(false);
            return "获取UUID失败，请重试登陆……";
        }
        LOG.info("获取得到UUID成功，UUID：" + uuid);

        weChatService.showQRCode(uuid);

        weChatService.pollForScanRes(uuid);

        // 登录成功后就把登录标志置为true
        // 第一次登陆时，将 false 置为 true，在没注销之前再次请求 UUID 都不会成功
        if (!locked.compareAndSet(false, true)) {
            return "不可重复登陆微信！";
        }

        weChatService.initializeweChat();

        weChatService.wxStatusNotify();

        weChatService.loadContactPerson();

        weChatService.listeningInMsg();

        return "success";
    }

    @Override
    public void doLogOut() {
        locked = new AtomicBoolean(false);
    }

    @Override
    public String initializeWeChatInfo() {

//        weChatService.initializeweChat();
//
//        weChatService.wxStatusNotify();
//
//        weChatService.loadContactPerson();
        return "success";
    }
}
