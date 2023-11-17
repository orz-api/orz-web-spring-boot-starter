package orz.springboot.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrzWebErrorRsp {
    private String code;
    private String reason;
    private List<OrzWebErrorTraceT1> traces;
}
