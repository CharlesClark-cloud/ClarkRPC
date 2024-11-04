package clark.nettyexercise;

import lombok.*;

/**
 * ClassName: RpcResponse
 * Package: com.clark.nettyexercise
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@ToString
public class RpcResponse {
    private String message;
}
