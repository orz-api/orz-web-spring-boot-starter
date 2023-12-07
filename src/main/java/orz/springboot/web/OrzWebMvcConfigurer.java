package orz.springboot.web;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import orz.springboot.alarm.exception.OrzAlarmException;
import orz.springboot.base.OrzBaseUtils;

import java.util.List;

import static orz.springboot.alarm.OrzAlarmUtils.alarm;

@Component
public class OrzWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new AlarmHandlerExceptionResolver());
    }

    private static class AlarmHandlerExceptionResolver implements HandlerExceptionResolver {
        @Nullable
        @Override
        public ModelAndView resolveException(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception ex) {
            OrzBaseUtils.getException(OrzAlarmException.class, ex).ifPresent(e -> alarm(e, ex));
            return null;
        }
    }
}
