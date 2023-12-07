package orz.springboot.web;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;
import orz.springboot.web.annotation.OrzWebErrors;
import orz.springboot.web.model.OrzWebProtocolBo;

import java.util.Objects;

import static orz.springboot.alarm.OrzAlarmUtils.alarm;
import static orz.springboot.base.OrzBaseUtils.hashMap;
import static orz.springboot.base.description.OrzDescriptionUtils.descTitles;

@RestControllerAdvice(annotations = {OrzWebApi.class})
public class OrzWebApiAdvice implements ResponseBodyAdvice<Object> {
    private static final Logger logger = LoggerFactory.getLogger("orz-web-api");

    private final OrzWebApiHandler handler;

    public OrzWebApiAdvice(OrzWebApiHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean supports(@Nonnull MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType, @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        return handler.processSuccessResponse(body, response);
    }

    @ExceptionHandler({OrzWebApiException.class})
    public Object handleWebApiException(Exception topException, HandlerMethod handler, HttpServletRequest request) throws Exception {
        var exception = OrzBaseUtils.getException(OrzWebApiException.class, topException).orElseThrow(() -> topException);
        var annotation = getErrorAnnotation(handler, exception.getCode());

        OrzWebProtocolBo protocol;
        String reason;
        if (annotation != null) {
            protocol = OrzWebProtocolBo.error(annotation.code(), annotation.notice());
            var desc = descTitles(annotation.reason()).merge(exception.getDescription());
            reason = StringUtils.defaultIfBlank(desc.toString(), null);
            if (annotation.alarm()) {
                alarm("@ORZ_WEB_ERROR_ALARM", reason, topException, hashMap(
                        "code", exception.getCode(),
                        "desc", exception.getDescription(),
                        "handler", handler.toString()
                ));
            }
            if (annotation.logging() && logger.isErrorEnabled()) {
                logger.error(desc.values("handler", handler).toString(), topException);
            }
        } else {
            protocol = OrzWebProtocolBo.error();
            var desc = descTitles("error undefined").values("code", exception.getCode()).merge(exception.getDescription());
            reason = desc.toString();
            alarm("@ORZ_WEB_ERROR_UNDEFINED", reason, topException, hashMap(
                    "code", exception.getCode(),
                    "desc", exception.getDescription(),
                    "handler", handler.toString()
            ));
            if (logger.isErrorEnabled()) {
                logger.error(desc.values("handler", handler).toString(), topException);
            }
        }
        return this.handler.buildErrorResponse(protocol, reason, null, topException, request);
    }

    private static OrzWebError getErrorAnnotation(HandlerMethod method, String code) {
        var errors = method.getMethodAnnotation(OrzWebErrors.class);
        if (errors != null) {
            for (var error : errors.value()) {
                if (Objects.equals(error.code(), code)) {
                    return error;
                }
            }
            return null;
        }
        var error = method.getMethodAnnotation(OrzWebError.class);
        if (error != null && Objects.equals(error.code(), code)) {
            return error;
        }
        return null;
    }
}
