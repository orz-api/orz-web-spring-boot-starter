package orz.springboot.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrzWebErrorTraceTo {
    private String service;
    private String endpoint;
    private String details;
}
