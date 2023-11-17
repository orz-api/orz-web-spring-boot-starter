package orz.springboot.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrzWebRequestHeadersB1 {
    private final String requestId;

    private final Long requestTime;

    private final Long userId;

    private final String clientIp;

    private final String clientType;

    private final Integer clientVersion;

    private final String clientChannel;

    private final Long launchTime;

    private final Integer launchScene;

    private final String deviceId;

    private final String deviceBrand;

    private final String deviceModel;

    private final String osType;

    private final String osName;

    private final String platformVersion;

    private final String platformSDKVersion;
}
