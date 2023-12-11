package orz.springboot.web.api.scope_v1;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.alarm.exception.OrzAlarmException;
import orz.springboot.alarm.exception.OrzUnexpectedException;
import orz.springboot.web.OrzWebException;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;

import static orz.springboot.base.OrzBaseUtils.hashMap;
import static orz.springboot.base.description.OrzDescriptionUtils.descValues;

@OrzWebApi(domain = "Test", action = "Find", variant = 1)
public class TestFindV1Api {
    @OrzWebError(code = "1", reason = "test 1")
    @OrzWebError(code = "2", reason = "test 2", alarm = true, logging = true)
    public TestFindV1ApiRsp request(@Validated @RequestBody TestFindV1ApiReq req) {
        if ("0".equals(req.getTest())) {
            throw new OrzAlarmException("test", hashMap("req", req));
        } else if ("1".equals(req.getTest())) {
            throw new OrzWebException("1", descValues("req", req));
        } else if ("2".equals(req.getTest())) {
            throw new OrzWebException("1", descValues("req", req), new OrzAlarmException("test", hashMap("req", req)));
        } else if ("3".equals(req.getTest())) {
            throw new OrzUnexpectedException("request error", hashMap("req", req));
        } else if ("4".equals(req.getTest())) {
            throw new OrzWebException("not_exists_code");
        } else if ("5".equals(req.getTest())) {
            throw new OrzWebException("2", descValues("req", req));
        }
        return new TestFindV1ApiRsp();
    }

    @Data
    public static class TestFindV1ApiReq {
        private String test;
    }

    @Data
    public static class TestFindV1ApiRsp {
    }
}
