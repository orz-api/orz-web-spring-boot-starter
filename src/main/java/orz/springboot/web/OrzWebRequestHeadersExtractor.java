package orz.springboot.web;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import orz.springboot.alarm.exception.OrzUnexpectedException;
import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.model.OrzWebRequestHeadersBo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Optional;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;

@Component
public class OrzWebRequestHeadersExtractor {
    private static final String REQUEST_HEADERS_ATTRIBUTE_NAME = "ORZ_WEB_REQUEST_HEADERS";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendValue(ChronoField.MILLI_OF_SECOND, 1, 9, SignStyle.NORMAL)
            .optionalEnd()
            .toFormatter(Locale.ENGLISH);

    private final OrzWebProps props;

    public OrzWebRequestHeadersExtractor(OrzWebProps props) {
        this.props = props;
    }

    public OrzWebRequestHeadersBo extract() {
        return extract(getCurrentRequest());
    }

    public OrzWebRequestHeadersBo extract(HttpServletRequest request) {
        return OrzBaseUtils.getRequestAttribute(REQUEST_HEADERS_ATTRIBUTE_NAME, OrzWebRequestHeadersBo.class)
                .orElseGet(() -> extractFromRequest(request));
    }

    private OrzWebRequestHeadersBo extractFromRequest(HttpServletRequest request) {
        var field = props.getRequestHeaders();
        var headers = new OrzWebRequestHeadersBo(
                getStringHeader(request, field.getRequestId(), field.isRequestIdRequired()),
                getDateTimeHeader(request, field.getRequestTime(), field.isRequestTimeRequired()),
                getStringHeader(request, field.getUserId(), field.isUserIdRequired()),
                getClientIp(request, field.getClientIp(), field.isClientIpRequired()),
                getStringHeader(request, field.getClientType(), field.isClientTypeRequired()),
                getIntHeader(request, field.getClientVersion(), field.isClientVersionRequired()),
                getStringHeader(request, field.getClientChannel(), field.isClientChannelRequired()),
                getDateTimeHeader(request, field.getInitialTime(), field.isInitialTimeRequired()),
                getDateTimeHeader(request, field.getLaunchTime(), field.isLaunchTimeRequired()),
                getStringHeader(request, field.getLaunchScene(), field.isLaunchSceneRequired()),
                getStringHeader(request, field.getDeviceId(), field.isDeviceIdRequired()),
                getStringHeader(request, field.getDeviceBrand(), field.isDeviceBrandRequired()),
                getStringHeader(request, field.getDeviceModel(), field.isDeviceModelRequired()),
                getStringHeader(request, field.getOsType(), field.isOsTypeRequired()),
                getStringHeader(request, field.getOsName(), field.isOsNameRequired()),
                getStringHeader(request, field.getPlatformVersion(), field.isPlatformVersionRequired()),
                getStringHeader(request, field.getPlatformSDKVersion(), field.isPlatformSDKVersionRequired())
        );
        OrzBaseUtils.setRequestAttribute(REQUEST_HEADERS_ATTRIBUTE_NAME, headers);
        return headers;
    }

    private static HttpServletRequest getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new OrzUnexpectedException("getCurrentRequest() is null"));
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

    private static LocalDateTime getDateTimeHeader(HttpServletRequest request, String headerName, boolean required) {
        var str = getStringHeader(request, headerName, required);
        if (str == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DATE_TIME_FORMATTER);
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
