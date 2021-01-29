package sunhongbin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by SunHongbin on 2021/1/29
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${thread-pool.corePoolSize:10}")
    private int corePoolSize;

    @Value("${thread-pool.maximumPoolSize:10}")
    private int maximumPoolSize;

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>());
    }

}
