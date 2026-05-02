package io.github.graceman.alicemvc.exception;

/**
 * Exception for business rule violations.
 *
 * <p>Throw this from hooks, services, or custom endpoints when a business
 * rule is violated. The framework's exception handler returns HTTP 422
 * (Unprocessable Entity) — semantically correct for "valid syntax, invalid
 * business logic".</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * @Override
 * protected void beforeCreate(HookContext<Order, Long> context) {
 *     Order order = context.getEntity();
 *     if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
 *         throw new BusinessException("Order total must be positive");
 *     }
 *     if (inventoryService.isOutOfStock(order.getProductId())) {
 *         throw new BusinessException("Product is out of stock");
 *     }
 * }
 * }</pre>
 *

 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
