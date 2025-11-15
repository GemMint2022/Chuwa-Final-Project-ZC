//package com.shop.order.exception;
//
//import com.shopping.common.BusinessException;
//
//public class OrderNotFoundException extends BusinessException {
//    public OrderNotFoundException(String message) {
//        super(message, "ORDER_NOT_FOUND");
//    }
//
//    public OrderNotFoundException(String orderId, String message) {
//        super(message, "ORDER_NOT_FOUND");
//    }
//
//    public OrderNotFoundException(String message, Throwable cause) {
//        super(message, "ORDER_NOT_FOUND", cause);
//    }
//}

package com.shop.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}