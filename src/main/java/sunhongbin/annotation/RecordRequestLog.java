package sunhongbin.annotation;

import sunhongbin.enums.RequestSourceEnum;

import java.lang.annotation.*;

/**
 * created by SunHongbin on 2021/2/3
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RecordRequestLog {

    RequestSourceEnum source() default RequestSourceEnum.FRONT_END;
}
