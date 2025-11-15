//package com.shop.order.exception;
//
//import com.shopping.common.BusinessException;
//
//public class ItemServiceException extends BusinessException {
//    public ItemServiceException(String message) {
//        super(message, "ITEM_SERVICE_ERROR");
//    }
//
//    public ItemServiceException(String message, Throwable cause) {
//        super(message, "ITEM_SERVICE_ERROR", cause);
//    }
//}

package com.shop.order.exception;

public class ItemServiceException extends RuntimeException {
    public ItemServiceException(String message) {
        super(message);
    }

    public ItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}