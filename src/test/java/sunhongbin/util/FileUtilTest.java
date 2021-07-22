package sunhongbin.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class FileUtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtilTest.class);

    @Test
    void getImageFilePath() {

        String str = "str";

        trans();

        System.out.println(str);
    }

    private void trans() {
        String str = "new Str";

        System.out.println(str);
    }
}