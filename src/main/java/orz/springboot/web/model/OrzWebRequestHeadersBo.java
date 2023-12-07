package orz.springboot.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrzWebRequestHeadersBo {
    private final String requestId;

    private final LocalDateTime requestTime;

    private final String userId;

    private final String clientIp;

    private final String clientType;

    private final Integer clientVersion;

    private final String clientChannel;

    private final LocalDateTime launchTime;

    private final String launchScene;

    private final String deviceId;

    private final String deviceBrand;

    private final String deviceModel;

    private final String osType;

    private final String osName;

    private final String platformVersion;

    private final String platformSDKVersion;
}
