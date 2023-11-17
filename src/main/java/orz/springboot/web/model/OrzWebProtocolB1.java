package orz.springboot.web.model;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import orz.springboot.web.OrzWebApiDefinition;

@Data
public class OrzWebProtocolB1 {
    private final int version;
    private final String code;
    private final String notice;

    public boolean isSuccess() {
        return StringUtils.isBlank(code);
    }

    public boolean codeEquals(String code) {
        return StringUtils.equals(this.code, code);
    }

    public static OrzWebProtocolB1 success() {
        return success(null);
    }

    public static OrzWebProtocolB1 success(Integer version) {
        return new OrzWebProtocolB1(
                ObjectUtils.defaultIfNull(version, OrzWebApiDefinition.VERSION_CURRENT),
                null,
                null
        );
    }

    public static OrzWebProtocolB1 error() {
        return error(null, null, null);
    }

    public static OrzWebProtocolB1 error(String code, String notice) {
        return error(null, code, notice);
    }

    public static OrzWebProtocolB1 error(Integer version, String code, String notice) {
        return new OrzWebProtocolB1(
                ObjectUtils.defaultIfNull(version, OrzWebApiDefinition.VERSION_CURRENT),
                StringUtils.defaultIfBlank(code, OrzWebApiDefinition.CODE_UNDEFINED),
                StringUtils.defaultIfBlank(notice, null)
        );
    }
}
