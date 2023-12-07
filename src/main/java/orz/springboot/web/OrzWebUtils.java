package orz.springboot.web;

import org.apache.commons.lang3.StringUtils;
import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.model.OrzWebRequestHeadersBo;

import java.util.Arrays;
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
        var packageName = cls.getPackageName();
        if (packageName.length() <= API_PACKAGE.length() || !packageName.startsWith(API_PACKAGE)) {
            return null;
        }
        var scope = packageName.substring(packageName.lastIndexOf('.') + 1);
        return Arrays.stream(scope.split("_")).map(StringUtils::capitalize).collect(Collectors.joining());
    }
}
