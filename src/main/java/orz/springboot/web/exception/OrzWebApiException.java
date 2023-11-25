package orz.springboot.web.exception;

import lombok.Getter;
import orz.springboot.base.description.OrzDescription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;
import static orz.springboot.base.description.OrzDescriptionUtils.descValues;

@Getter
public class OrzWebApiException extends RuntimeException {
    private final String code;
    private final OrzDescription description;

    public OrzWebApiException(@Nonnull String code) {
        this(code, null, null);
    }

    public OrzWebApiException(@Nonnull String code, @Nullable OrzDescription description) {
        this(code, description, null);
    }

    public OrzWebApiException(@Nonnull String code, @Nullable OrzDescription description, @Nullable Throwable cause) {
        super(buildMessage(code, description), cause);
        this.code = code;
        this.description = description;
    }

    private static String buildMessage(String code, OrzDescription description) {
        if (description == null) {
            return desc(null, "code", code);
        }
        return descValues("code", code).merge(description).toString();
    }
}
