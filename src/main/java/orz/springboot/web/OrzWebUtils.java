package orz.springboot.web;

import orz.springboot.base.OrzBaseUtils;
import orz.springboot.web.model.OrzWebRequestHeadersBo;

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
}
