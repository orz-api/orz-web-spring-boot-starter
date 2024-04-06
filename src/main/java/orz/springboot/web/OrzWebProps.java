package orz.springboot.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "orz.web")
public class OrzWebProps {
    @Value("${orz.web.service:${spring.application.name:unnamed}}")
    @NotNull
    private String service = "unnamed";

    @Value("${orz.web.expose-error-reason:#{'${server.error.include-message:never}'.toLowerCase() == 'always'}}")
    @NotNull
    private boolean exposeErrorReason = false;

    @Value("${orz.web.expose-error-traces:#{'${server.error.include-stacktrace:never}'.toLowerCase() == 'always'}}")
    @NotNull
    private boolean exposeErrorTraces = false;

    @Valid
    @NotNull
    private RequestHeadersConfig requestHeaders = new RequestHeadersConfig();

    @Valid
    @NotNull
    private ResponseHeadersConfig responseHeaders = new ResponseHeadersConfig();

    @Valid
    @NotNull
    private PageConfig page = new PageConfig();

    @Data
    public static class RequestHeadersConfig {
        @NotBlank
        private String requestId = "Orz-Request-Id";

        private boolean requestIdRequired = true;

        @NotBlank
        private String requestTime = "Orz-Request-Time";

        private boolean requestTimeRequired = true;

        @NotBlank
        private String userId = "Orz-User-Id";

        private boolean userIdRequired = false;

        @NotBlank
        private String clientIp = "Orz-Client-Ip";

        private boolean clientIpRequired = true;

        @NotBlank
        private String clientType = "Orz-Client-Type";

        private boolean clientTypeRequired = true;

        @NotBlank
        private String clientVersion = "Orz-Client-Version";

        private boolean clientVersionRequired = true;

        @NotBlank
        private String clientChannel = "Orz-Client-Channel";

        private boolean clientChannelRequired = false;

        @NotBlank
        private String initialTime = "Orz-Initial-Time";

        private boolean initialTimeRequired = true;

        @NotBlank
        private String launchTime = "Orz-Launch-Time";

        private boolean launchTimeRequired = true;

        @NotBlank
        private String launchScene = "Orz-Launch-Scene";

        private boolean launchSceneRequired = false;

        @NotBlank
        private String deviceId = "Orz-Device-Id";

        private boolean deviceIdRequired = true;

        @NotBlank
        private String deviceBrand = "Orz-Device-Brand";

        private boolean deviceBrandRequired = false;

        @NotBlank
        private String deviceModel = "Orz-Device-Model";

        private boolean deviceModelRequired = false;

        @NotBlank
        private String osType = "Orz-OS-Type";

        private boolean osTypeRequired = false;

        @NotBlank
        private String osName = "Orz-OS-Name";

        private boolean osNameRequired = false;

        @NotBlank
        private String platformVersion = "Orz-Platform-Version";

        private boolean platformVersionRequired = false;

        @NotBlank
        private String platformSDKVersion = "Orz-Platform-SDK-Version";

        private boolean platformSDKVersionRequired = false;
    }

    @Data
    public static class ResponseHeadersConfig {
        @NotBlank
        private String version = "Orz-Version";

        @NotBlank
        private String code = "Orz-Code";

        @NotBlank
        private String notice = "Orz-Notice";
    }

    @Data
    public static class PageConfig {
        @NotNull
        @Positive
        private Integer defaultSize = 50;

        @NotNull
        @Positive
        private Integer maxSize = 100;
    }
}
