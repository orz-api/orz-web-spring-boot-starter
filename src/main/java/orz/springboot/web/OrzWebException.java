package orz.springboot.web;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import orz.springboot.base.description.OrzDescription;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;
import static orz.springboot.base.description.OrzDescriptionUtils.descValues;

@Getter
public class OrzWebException extends RuntimeException {
    private final String code;
    private final OrzDescription description;

    public OrzWebException(@Nonnull String code) {
        this(code, null, null);
    }

    public OrzWebException(@Nonnull String code, @Nullable OrzDescription description) {
        this(code, description, null);
    }

    public OrzWebException(@Nonnull String code, @Nullable OrzDescription description, @Nullable Throwable cause) {
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
