package orz.springboot.web;

import org.apache.commons.lang3.StringUtils;
import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.model.OrzWebRequestHeadersBo;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static orz.springboot.web.OrzWebConstants.API_PACKAGE;

public class OrzWebUtils {
    public static int adjustPageSize(Integer size) {
        var page = OrzBaseUtils.getAppContext().getBean(OrzWebProps.class).getPage();
        var pageSize = size != null && size > 0 ? size : page.getDefaultSize();
        if (pageSize > page.getMaxSize()) {
            return page.getMaxSize();
        }
        return pageSize;
    }

    public static OrzWebRequestHeadersBo extractRequestHeaders() {
        return OrzBaseUtils.getAppContext().getBean(OrzWebRequestHeadersExtractor.class).extract();
    }

    public static String getClientType() {
        return extractRequestHeaders().getClientType();
    }

    public static String getScope(Class<?> cls) {
        return getScopeFromPackage(cls.getPackageName());
    }

    public static String getScope(String className) {
        return Optional.ofNullable(className)
                .filter(s -> s.contains("."))
                .map(s -> s.substring(0, s.lastIndexOf(".")))
                .map(OrzWebUtils::getScopeFromPackage)
                .orElse(null);
    }

    public static String getScopeFromPackage(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return null;
        }
        var packageArray = packageName.split("\\.");
        if (packageArray.length < 2) {
            return null;
        }
        var scope = (String) null;
        for (int i = packageArray.length - 1; i >= 0; i--) {
            if (API_PACKAGE.equals(packageArray[i])) {
                if (i + 1 < packageArray.length) {
                    scope = packageArray[i + 1];
                    break;
                } else {
                    return null;
                }
            }
        }
        if (scope == null) {
            return null;
        }
        return Arrays.stream(scope.split("_")).map(StringUtils::capitalize).collect(Collectors.joining());
    }
}
