package orz.springboot.web;

import org.springframework.web.method.HandlerMethod;
import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.annotation.OrzWebError;
import orz.springboot.web.annotation.OrzWebErrors;
import orz.springboot.web.model.OrzWebRequestHeadersB1;

import java.util.Objects;
import java.util.Optional;

public class OrzWebUtils {
    public static <T extends Throwable> Optional<T> getException(Class<T> instanceClass, Throwable top) {
        T result = null;

        Throwable throwable = top;
        for (int i = 0; i < 10; i++) {
            if (instanceClass.isInstance(throwable)) {
                // noinspection unchecked
                result = (T) throwable;
                break;
            }

            if (throwable.getCause() != null) {
                throwable = throwable.getCause();
            } else {
                break;
            }
        }

        return Optional.ofNullable(result);
    }

    public static OrzWebError getErrorAnnotation(HandlerMethod method, String code) {
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

    public static int adjustPageSize(Integer size) {
        var page = OrzBaseUtils.getAppContext().getBean(OrzWebProps.class).getPage();
        var pageSize = size != null && size > 0 ? size : page.getDefaultSize();
        if (pageSize > page.getMaxSize()) {
            return page.getMaxSize();
        }
        return pageSize;
    }

    public static OrzWebRequestHeadersB1 extractRequestHeaders() {
        return OrzBaseUtils.getAppContext().getBean(OrzWebRequestHeadersExtractor.class).extract();
    }
}
