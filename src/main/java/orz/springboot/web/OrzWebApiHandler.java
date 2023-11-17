package orz.springboot.web;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import orz.springboot.web.model.OrzWebProtocolB1;
import orz.springboot.web.model.OrzWebErrorRsp;
import orz.springboot.web.model.OrzWebErrorTraceT1;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class OrzWebApiHandler {
    private static final Logger alarmLog = LoggerFactory.getLogger("orz-alarm");
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
        response.getHeaders().set(props.getResponseHeaders().getVersion(), String.valueOf(OrzWebApiDefinition.VERSION_CURRENT));
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
    public ResponseEntity<?> buildErrorResponse(OrzWebProtocolB1 protocol, String reason, @Nullable List<OrzWebErrorTraceT1> extraTraces, Exception topException, HttpServletRequest request) {
        String exposeReason = null;
        if (props.isExposeErrorReason()) {
            exposeReason = reason;
        }

        List<OrzWebErrorTraceT1> exposeTraces = null;
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

    /**
     * 报告错误
     * <p>
     * 将进行记录日志、上报运维追踪平台、发送告警等操作
     *
     * @param alarm        是否告警
     * @param logging      是否输出日志
     * @param reason       错误原因
     * @param topException 最顶层的异常
     * @param method       出错的方法
     * @param logger       日志记录器
     * @param logLevel     日志级别
     */
    public void reportError(boolean alarm, boolean logging, String reason, Exception topException, HandlerMethod method, Logger logger, Level logLevel) {
        if (alarm) {
            // TODO: report to other monitor platform
            alarmLog.error("orz-api [{}.{}] threw [{}] exception",
                    method.getMethod().getDeclaringClass().getSimpleName(),
                    method.getMethod().getName(),
                    reason,
                    topException
            );
        }

        if (logging) {
            if (logger.isEnabledForLevel(logLevel)) {
                logger.atLevel(logLevel).log("orz-api [{}.{}] threw [{}] exception",
                        method.getMethod().getDeclaringClass().getSimpleName(),
                        method.getMethod().getName(),
                        reason,
                        topException
                );
            }
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

    public List<OrzWebErrorTraceT1> getTraces(HttpServletRequest request, Exception topException, @Nullable List<OrzWebErrorTraceT1> extraTraces) {
        var traces = new ArrayList<OrzWebErrorTraceT1>();
        traces.add(new OrzWebErrorTraceT1(props.getService(), getEndpoint(request), ExceptionUtils.getStackTrace(topException)));
        if (!CollectionUtils.isEmpty(extraTraces)) {
            traces.addAll(extraTraces);
        }
        return traces;
    }
}
