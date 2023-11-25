package orz.springboot.web;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import orz.springboot.web.model.OrzWebRequestHeadersBo;

import java.util.Optional;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;

@Component
public class OrzWebRequestHeadersExtractor {
    private static final String REQUEST_HEADERS_ATTRIBUTE_NAME = OrzWebRequestHeadersBo.class.getName();

    private final OrzWebProps props;

    public OrzWebRequestHeadersExtractor(OrzWebProps props) {
        this.props = props;
    }

    public OrzWebRequestHeadersBo extract() {
        var request = getCurrentRequest();
        if (request == null) {
            throw new ResponseStatusException(500, "current request is null", null);
        }
        return extract(request);
    }

    public OrzWebRequestHeadersBo extract(HttpServletRequest request) {
        return extractFromRequestContext().orElseGet(() -> extractFromRequest(request));
    }

    private OrzWebRequestHeadersBo extractFromRequest(HttpServletRequest request) {
        var field = props.getRequestHeaders();
        var headers = new OrzWebRequestHeadersBo(
                getStringHeader(request, field.getRequestId(), field.isRequestIdRequired()),
                getLongHeader(request, field.getRequestTime(), field.isRequestTimeRequired()),
                getLongHeader(request, field.getUserId(), field.isUserIdRequired()),
                getClientIp(request, field.getClientIp(), field.isClientIpRequired()),
                getStringHeader(request, field.getClientType(), field.isClientTypeRequired()),
                getIntHeader(request, field.getClientVersion(), field.isClientVersionRequired()),
                getStringHeader(request, field.getClientChannel(), field.isClientChannelRequired()),
                getLongHeader(request, field.getLaunchTime(), field.isLaunchTimeRequired()),
                getIntHeader(request, field.getLaunchScene(), field.isLaunchSceneRequired()),
                getStringHeader(request, field.getDeviceId(), field.isDeviceIdRequired()),
                getStringHeader(request, field.getDeviceBrand(), field.isDeviceBrandRequired()),
                getStringHeader(request, field.getDeviceModel(), field.isDeviceModelRequired()),
                getStringHeader(request, field.getOsType(), field.isOsTypeRequired()),
                getStringHeader(request, field.getOsName(), field.isOsNameRequired()),
                getStringHeader(request, field.getPlatformVersion(), field.isPlatformVersionRequired()),
                getStringHeader(request, field.getPlatformSDKVersion(), field.isPlatformSDKVersionRequired())
        );
        return saveToRequestContext(headers);
    }

    private Optional<OrzWebRequestHeadersBo> extractFromRequestContext() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(attrs -> attrs.getAttribute(REQUEST_HEADERS_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST))
                .map(OrzWebRequestHeadersBo.class::cast);
    }

    private OrzWebRequestHeadersBo saveToRequestContext(OrzWebRequestHeadersBo headers) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(REQUEST_HEADERS_ATTRIBUTE_NAME, headers, RequestAttributes.SCOPE_REQUEST);
        }
        return headers;
    }

    private static HttpServletRequest getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElse(null);
    }

    private static String getStringHeader(HttpServletRequest request, String headerName, boolean required) {
        var headerValue = request.getHeader(headerName);
        if (StringUtils.isBlank(headerValue)) {
            if (required) {
                throw new ResponseStatusException(400, desc("header not found", "header", headerName), null);
            } else {
                return null;
            }
        }
        return headerValue;
    }

    private static Long getLongHeader(HttpServletRequest request, String headerName, boolean required) {
        var str = getStringHeader(request, headerName, required);
        if (str == null) {
            return null;
        }
        try {
            return Long.valueOf(str);
        } catch (Exception e) {
            throw new ResponseStatusException(400, desc("header is invalid", "header", headerName, "value", str), null);
        }
    }

    private static Integer getIntHeader(HttpServletRequest request, String headerName, boolean required) {
        var str = getStringHeader(request, headerName, required);
        if (str == null) {
            return null;
        }
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            throw new ResponseStatusException(400, desc("header is invalid", "header", headerName, "value", str), null);
        }
    }

    private static String getClientIp(HttpServletRequest request, String headerName, boolean required) {
        var ip = getStringHeader(request, headerName, false);
        if (ip == null) {
            var forwarded = getStringHeader(request, "X-Forwarded-For", false);
            if (StringUtils.isNotBlank(forwarded)) {
                var first = forwarded.split(",")[0];
                if (StringUtils.isNotBlank(first)) {
                    ip = first;
                }
            }
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        if (ip == null) {
            if (required) {
                throw new ResponseStatusException(400, desc("header not found", "header", headerName), null);
            } else {
                return null;
            }
        }
        return ip;
    }
}
