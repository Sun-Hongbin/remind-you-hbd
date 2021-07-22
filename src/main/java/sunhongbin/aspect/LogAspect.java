package sunhongbin.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sunhongbin.annotation.RecordRequestLog;
import sunhongbin.entity.SystemLog;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * created by SunHongbin on 2021/2/3
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private ExecutorService executorService;

    /**
     * first * : any controller
     * second * : any method in any controller
     * (..) : any parameter
     */
    @Pointcut("execution(* sunhongbin.controller.*.*(..))")
    private void forControllerPackage() {
    }

    @Pointcut("execution(* sunhongbin.service.*.*(..))")
    private void forServicePackage() {
    }

    @Pointcut("forControllerPackage() || forServicePackage()")
    private void forAppFlow() {
    }

    @Pointcut("@annotation(recordRequestLog)")
    public void forLogAnnotation(sunhongbin.annotation.RecordRequestLog recordRequestLog) {
    }

    @Around("forLogAnnotation(recordRequestLog)")
    public Object recordRequestLog(ProceedingJoinPoint joinPoint, RecordRequestLog recordRequestLog) throws Throwable {

        // display the method we are calling
        String theMethod = joinPoint.getSignature().toLongString();
        LOG.info("=====>> Executing @Around on method: " + theMethod);

        // get begin timestamp
        long begin = System.currentTimeMillis();

        // get the arguments
        Object[] args = joinPoint.getArgs();
        LOG.info("=====>> in @Around: argument: " + Arrays.toString(args));

        // now, let's execute the method
        Object result = null;
        String response;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            response = result.toString();
        } catch (Exception e) {
            // log the exception
            LOG.error(e.getMessage());
            // add exception message to 'result' to save in ZCC_SYSTEM_LOG_T
            response = e.getMessage();
            // rethrow e
            exception = e;
        }
        LOG.info("=====>> in @Around: result: " + response);

        // get end timestamp
        long end = System.currentTimeMillis();

        // compute duration and save it
        double duration = (end - begin) / 1000.0;

        // save
        final String rsp = response;
        CompletableFuture.runAsync(() -> {
            SystemLog systemLog = SystemLog.builder()
                    .source(recordRequestLog.source())
                    .request(Arrays.toString(args))
                    .response(rsp)
                    .apiName(theMethod)
                    .duration(duration)
                    .build();
            LOG.info(systemLog.toString());
        }, executorService);

        // rethrow exception
        if (exception != null) {
            throw exception;
        }

        return result;
    }
}