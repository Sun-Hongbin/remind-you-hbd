package sunhongbin.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sunhongbin.service.SmartRobotService;

@SpringBootTest
class SmartRobotServiceImplTest {

    @Autowired
    private SmartRobotService smartRobotService;

    @Test
    void aiReply() {
        String res = smartRobotService.aiReply("你好~");
        System.out.println(res);
    }

}