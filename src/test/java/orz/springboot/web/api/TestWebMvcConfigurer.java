package orz.springboot.web.api;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import orz.springboot.alarm.exception.OrzAlarmException;
import orz.springboot.alarm.exception.OrzUnexpectedException;
import orz.springboot.web.OrzWebApiException;

import static orz.springboot.base.OrzBaseUtils.hashMap;
import static orz.springboot.base.description.OrzDescriptionUtils.descValues;

@Component
public class TestWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TestInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
    }

    private static class TestInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler) throws Exception {
            if (request.getRequestURI().equals("/error")) {
                return true;
            }
            if ("0".equals(request.getParameter("test"))) {
                throw new OrzAlarmException("TestInterceptor", hashMap("handler", handler));
            } else if ("1".equals(request.getParameter("test"))) {
                throw new OrzWebApiException("1", descValues("handler", handler));
            } else if ("2".equals(request.getParameter("test"))) {
                throw new OrzWebApiException("1", descValues("handler", handler), new OrzAlarmException("TestInterceptor", hashMap("handler", handler)));
            } else if ("3".equals(request.getParameter("test"))) {
                throw new OrzUnexpectedException("TestInterceptor error", hashMap("handler", handler));
            } else if ("4".equals(request.getParameter("test"))) {
                throw new ResponseStatusException(401, "Unauthorized", new OrzAlarmException("TestInterceptor", hashMap("handler", handler)));
            }
            return true;
        }
    }
}
