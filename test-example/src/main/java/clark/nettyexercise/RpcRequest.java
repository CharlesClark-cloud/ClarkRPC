package clark.nettyexercise;

import lombok.*;

/**
 * ClassName: RpcRequest
 * Package: com.clark.nettyexercise
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@ToString
public class RpcRequest {
    private String interfaceName;
    private String methodName;
}
