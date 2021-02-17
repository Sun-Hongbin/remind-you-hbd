package sunhongbin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sunhongbin.enums.WeChatApi;
import sunhongbin.service.SmartRobotService;
import sunhongbin.service.WeChatService;
import sunhongbin.util.DateUtil;
import sunhongbin.util.HttpUtil;

import javax.annotation.Resource;

/**
 * created by SunHongbin on 2020/4/27
 */
@Service
public class SmartRobotServiceImpl implements SmartRobotService {

    @Resource
    private WeChatService weChatService;

    @Override
    public String aiReply(String sendMessage) {

        JSONObject param = new JSONObject();

        param.put("key", "80a7ba9246814892ad0836b6561be745");

        param.put("info", sendMessage);

        String aiReplyRes = HttpUtil.doPost(WeChatApi.TU_LING_ROBOT.getUrl(), param);

        JSONObject jsonObject = JSON.parseObject(aiReplyRes);

        return jsonObject.getString("text");
    }

    @Override
    @Scheduled(cron = "${schedule.satisfy.drinkWater}")
    public void notifyDrinkWater() {

        String msg = "现在是：" + DateUtil.getCurrentTime("HH时mm分") + "，别忘了一天2L水的小目标噢~";


//        weChatService.sendMsgToWeChatFriend(msg, MemberTypeEnum.GROUP);
    }
}



