package orz.springboot.web.model;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import orz.springboot.web.OrzWebApiConstants;

@Data
public class OrzWebProtocolBo {
    private final int version;
    private final String code;
    private final String notice;

    public boolean isSuccess() {
        return StringUtils.isBlank(code);
    }

    public boolean codeEquals(String code) {
        return StringUtils.equals(this.code, code);
    }

    public static OrzWebProtocolBo success() {
        return success(null);
    }

    public static OrzWebProtocolBo success(Integer version) {
        return new OrzWebProtocolBo(
                ObjectUtils.defaultIfNull(version, OrzWebApiConstants.VERSION_CURRENT),
                null,
                null
        );
    }

    public static OrzWebProtocolBo error() {
        return error(null, null, null);
    }

    public static OrzWebProtocolBo error(String code, String notice) {
        return error(null, code, notice);
    }

    public static OrzWebProtocolBo error(Integer version, String code, String notice) {
        return new OrzWebProtocolBo(
                ObjectUtils.defaultIfNull(version, OrzWebApiConstants.VERSION_CURRENT),
                StringUtils.defaultIfBlank(code, OrzWebApiConstants.CODE_UNDEFINED),
                StringUtils.defaultIfBlank(notice, null)
        );
    }
}
