package sunhongbin.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sunhongbin.enums.MemberTypeEnum;
import sunhongbin.service.SmartRobotService;
import sunhongbin.service.WeChatService;
import sunhongbin.util.DateUtil;

import javax.annotation.Resource;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class SmartRobotServiceImpl implements SmartRobotService {

    private final Logger logger = LoggerFactory.getLogger(SmartRobotServiceImpl.class);

    @Resource
    private WeChatService weChatService;

    @Override
    public void listeningInMsg() {

    }

    @Override
    public void sendMsgToWeChatFriend() {

    }

    @Override
//    @Scheduled(cron = "${schedule.satisfy.drinkWater}")
    public void notifyDrinkWater() {
        logger.info("------------定时喝水提醒------------");
        String msg = "现在是：" + DateUtil.getCurrentTime("HH时mm分")
                   + "，别忘了一天3L水的小目标噢~";
        weChatService.sendMsgToWeChatFriend(msg, MemberTypeEnum.GROUP);
        logger.info("------------定时喝水提醒执行完毕------------");
    }
}



