package orz.springboot.web.exception;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static orz.springboot.base.OrzBaseUtils.message;

@Getter
public class OrzWebException extends RuntimeException {
    private final String code;
    private final Object[] extras;

    public OrzWebException(String message, Throwable cause, String code, Object... extras) {
        super(StringUtils.defaultIfBlank(message, message(null, "code", code, "extras", extras)), cause);
        this.code = code;
        this.extras = extras;
    }

    public OrzWebException(String code, Object... extras) {
        this(null, null, code, extras);
    }
}
