//package com.shop.order.exception;
//
//import com.shopping.common.BusinessException;
//
//public class InvalidOrderStateException extends BusinessException {
//    public InvalidOrderStateException(String message) {
//        super(message, "INVALID_ORDER_STATE");
//    }
//
//    public InvalidOrderStateException(String message, Throwable cause) {
//        super(message, "INVALID_ORDER_STATE", cause);
//    }
//}

package com.shop.order.exception;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}