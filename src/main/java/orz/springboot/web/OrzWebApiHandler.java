package orz.springboot.web;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import orz.springboot.web.model.OrzWebErrorRsp;
import orz.springboot.web.model.OrzWebErrorTraceTo;
import orz.springboot.web.model.OrzWebProtocolBo;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OrzWebApiHandler {
    private final OrzWebProps props;

    public OrzWebApiHandler(OrzWebProps props) {
        this.props = props;
    }

    /**
     * 处理成功的响应
     *
     * @param body     响应体
     * @param response 响应
     * @return 响应体
     */
    public Object processSuccessResponse(@Nullable Object body, @Nonnull ServerHttpResponse response) {
        response.getHeaders().set(props.getResponseHeaders().getVersion(), String.valueOf(OrzWebConstants.VERSION_CURRENT));
        return body;
    }

    /**
     * 构建错误响应
     *
     * @param protocol     协议
     * @param reason       错误原因
     * @param extraTraces  额外的追踪信息
     * @param topException 最顶层的异常
     * @param request      请求
     * @return 错误响应
     */
    public ResponseEntity<?> buildErrorResponse(OrzWebProtocolBo protocol, String reason, @Nullable List<OrzWebErrorTraceTo> extraTraces, Exception topException, HttpServletRequest request) {
        String exposeReason = null;
        if (props.isExposeErrorReason()) {
            exposeReason = reason;
        }

        List<OrzWebErrorTraceTo> exposeTraces = null;
        if (props.isExposeErrorTraces()) {
            exposeTraces = getTraces(request, topException, extraTraces);
        }

        var headers = new HttpHeaders();
        headers.set(props.getResponseHeaders().getVersion(), String.valueOf(protocol.getVersion()));
        headers.set(props.getResponseHeaders().getCode(), protocol.getCode());
        if (StringUtils.isNotBlank(protocol.getNotice())) {
            headers.set(props.getResponseHeaders().getNotice(), URLEncoder.encode(protocol.getNotice(), UTF_8));
        }

        if (StringUtils.isNotBlank(exposeReason) || !CollectionUtils.isEmpty(exposeTraces)) {
            var errorRsp = new OrzWebErrorRsp(protocol.getCode(), exposeReason, exposeTraces);
            return new ResponseEntity<>(errorRsp, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, headers, HttpStatus.OK);
        }
    }

    public String getEndpoint(HttpServletRequest request) {
        var contextPath = request.getContextPath();
        var endpoint = request.getRequestURI();
        if (StringUtils.isNotEmpty(contextPath) && endpoint.startsWith(contextPath)) {
            endpoint = endpoint.substring(contextPath.length());
        }
        return endpoint;
    }

    public List<OrzWebErrorTraceTo> getTraces(HttpServletRequest request, Exception topException, @Nullable List<OrzWebErrorTraceTo> extraTraces) {
        var traces = new ArrayList<OrzWebErrorTraceTo>();
        traces.add(new OrzWebErrorTraceTo(props.getService(), getEndpoint(request), ExceptionUtils.getStackTrace(topException)));
        if (!CollectionUtils.isEmpty(extraTraces)) {
            traces.addAll(extraTraces);
        }
        return traces;
    }
}
