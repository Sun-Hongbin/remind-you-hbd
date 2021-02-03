package sunhongbin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sunhongbin.enums.RequestSourceEnum;

import java.util.Date;

/**
 * created by SunHongbin on 2021/2/3
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemLog {

    private RequestSourceEnum source;

    private String request;

    private String response;

    private String apiName;

    private double duration;

    private Date crtTime;
}
