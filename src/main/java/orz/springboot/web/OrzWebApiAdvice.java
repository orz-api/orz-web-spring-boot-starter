package orz.springboot.web;

import com.google.common.base.Strings;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.exception.OrzWebException;
import orz.springboot.web.model.OrzWebProtocolB1;

import java.util.Arrays;

import static orz.springboot.base.OrzBaseUtils.message;

@Slf4j
@RestControllerAdvice(annotations = {OrzWebApi.class})
public class OrzWebApiAdvice implements ResponseBodyAdvice<Object> {
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

    @ExceptionHandler({OrzWebException.class})
    public Object handleException(Exception topException, HandlerMethod method, HttpServletRequest request) throws Exception {
        var exception = OrzWebUtils.getException(OrzWebException.class, topException).orElseThrow(() -> topException);
        var errorAnnotation = OrzWebUtils.getErrorAnnotation(method, exception.getCode());

        OrzWebProtocolB1 protocol;
        String reason;
        if (errorAnnotation != null) {
            protocol = OrzWebProtocolB1.error(errorAnnotation.code(), errorAnnotation.notice());
            reason = StringUtils.isNotEmpty(errorAnnotation.reason())
                    ? Strings.lenientFormat(errorAnnotation.reason(), exception.getExtras())
                    : null;
            handler.reportError(errorAnnotation.alarm(), errorAnnotation.logging(), reason, topException, method, log, Level.ERROR);
        } else {
            protocol = OrzWebProtocolB1.error();
            reason = message("undefined error", "code", exception.getCode(), "extras", Arrays.toString(exception.getExtras()));
            handler.reportError(true, true, reason, topException, method, log, Level.ERROR);
        }
        return handler.buildErrorResponse(protocol, reason, null, topException, request);
    }
}
