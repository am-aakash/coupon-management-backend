package com.example.CouponManagement.service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.CouponManagement.entity.Cart;
import com.example.CouponManagement.entity.CartItem;
import com.example.CouponManagement.entity.Coupon;
import com.example.CouponManagement.repository.CouponRepository;
import com.example.CouponManagement.util.CouponExpiredException;
import com.example.CouponManagement.util.CouponNotFoundException;

public class CouponService {

    private CouponRepository couponRepository = new CouponRepository();

    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.addCoupon(coupon);
    }

    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.getCouponById(id)
                .filter(coupon -> {
                    if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
                        throw new CouponExpiredException("Coupon with ID " + id + " is expired.");
                    }
                    return true;
                });
    }

    public Map<Long, Coupon> getAllCoupons() {
        return couponRepository.getAllCoupons().entrySet().stream()
                .filter(entry -> entry.getValue().getExpirationDate() == null ||
                        !entry.getValue().getExpirationDate().isBefore(LocalDate.now()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
        if (!couponRepository.getCouponById(id).isPresent()) {
            throw new CouponNotFoundException("Coupon with ID " + id + " not found.");
        }
        return couponRepository.updateCoupon(id, updatedCoupon);
    }

    public void deleteCoupon(Long id) {
        if (!couponRepository.getCouponById(id).isPresent()) {
            throw new CouponNotFoundException("Coupon with ID " + id + " not found.");
        }
        couponRepository.deleteCoupon(id);
    }

    public List<Coupon> getApplicableCoupons(Cart cart) {
        List<Coupon> applicableCoupons = new ArrayList<>();
        Map<Long, Coupon> allCoupons = getAllCoupons();

        for (Coupon coupon : allCoupons.values()) {
            if (isCouponApplicable(coupon, cart)) {
                applicableCoupons.add(coupon);
            }
        }

        return applicableCoupons;
    }

    public Cart applyCoupon(Long couponId, Cart cart) {
        Coupon coupon = getCouponById(couponId).orElseThrow(() ->
                new CouponNotFoundException("Coupon with ID " + couponId + " not found."));
        if (!isCouponApplicable(coupon, cart)) {
            throw new IllegalArgumentException("Coupon with ID " + couponId + " is not applicable.");
        }

        return applyCouponToCart(coupon, cart);
    }

	private boolean isCouponApplicable(Coupon coupon, Cart cart) {
        String type = coupon.getType();
        Map<String, Object> details = coupon.getDetails();
        List<CartItem> cartItems = cart.getItems();

        switch (type) {
            case "cart-wise":
                double threshold = (Integer) details.get("threshold");
                double cartTotal = (cartItems==null || cartItems.isEmpty())? 0.0 :  cart.getItems().stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();
                return cartTotal > threshold;

            case "product-wise":
                Long productId = ((Number) details.get("product_id")).longValue();
                return (cartItems==null || cartItems.isEmpty())? false :  cart.getItems().stream().anyMatch(item -> item.getProductId().equals(productId));

            case "bxgy":
                List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
                List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
                int repetitionLimit = (int) details.get("repetition_limit");

                int applicableRepetitions = calculateBxGyRepetitions(cart, buyProducts);
                return applicableRepetitions > 0 && applicableRepetitions <= repetitionLimit;

            default:
                return false;
        }
    }

    private int calculateBxGyRepetitions(Cart cart, List<Map<String, Object>> buyProducts) {
    	List<CartItem> cartItems = cart.getItems();
        Map<Long, Integer> cartProductQuantities = (cartItems==null || cartItems.isEmpty())? null : cartItems.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity, Integer::sum));

        int repetitions = Integer.MAX_VALUE;

        for (Map<String, Object> buyProduct : buyProducts) {
            Long productId = ((Number) buyProduct.get("product_id")).longValue();
            int requiredQuantity = (int) buyProduct.get("quantity");

            if (!cartProductQuantities.containsKey(productId)) {
                return 0;
            }

            int availableQuantity = cartProductQuantities.get(productId);
            repetitions = Math.min(repetitions, availableQuantity / requiredQuantity);
        }

        return repetitions;
    }

    private Cart applyCouponToCart(Coupon coupon, Cart cart) {
        String type = coupon.getType();
        Map<String, Object> details = coupon.getDetails();

        switch (type) {
            case "cart-wise":
                double discountPercentage = (double) details.get("discount");
                List<CartItem> cartItems = cart.getItems();
                double cartTotal = (cartItems==null || cartItems.isEmpty())? 0.0 : cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();
                double discountAmount = (cartTotal * discountPercentage) / 100;

                cart.setTotalPrice(cartTotal);
                cart.setTotalDiscount(discountAmount);
                cart.setFinalPrice(cartTotal - discountAmount);
                break;

            case "product-wise":
                Long productId = ((Number) details.get("product_id")).longValue();
                double productDiscount = (double) details.get("discount");

                cart.getItems().forEach(item -> {
                    if (item.getProductId().equals(productId)) {
                        double itemDiscount = (item.getPrice() * productDiscount) / 100;
                        item.setTotalDiscount(itemDiscount);
                        item.setPrice(item.getPrice() - itemDiscount);
                    }
                });

                updateCartTotals(cart);
                break;

            case "bxgy":
                List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
                List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
                int repetitionLimit = (int) details.get("repetition_limit");

                int repetitions = calculateBxGyRepetitions(cart, buyProducts);

                for (int i = 0; i < Math.min(repetitions, repetitionLimit); i++) {
                    for (Map<String, Object> getProduct : getProducts) {
                        Long freeProductId = ((Number) getProduct.get("product_id")).longValue();
                        int freeQuantity = (int) getProduct.get("quantity");

                        cartItems = cart.getItems();
                        Optional<CartItem> cartItemOptional = (cartItems==null || cartItems.isEmpty())? null : cart.getItems().stream()
                                .filter(item -> item.getProductId().equals(freeProductId))
                                .findFirst();

                        if (cartItemOptional.isPresent()) {
                            CartItem cartItem = cartItemOptional.get();
                            cartItem.setQuantity(cartItem.getQuantity() + freeQuantity);
                            cartItem.setTotalDiscount(cartItem.getTotalDiscount() + (cartItem.getPrice() * freeQuantity));
                        } else {
                            CartItem newCartItem = new CartItem();
                            newCartItem.setProductId(freeProductId);
                            newCartItem.setQuantity(freeQuantity);
                            newCartItem.setPrice(0); // Free item
                            newCartItem.setTotalDiscount(freeQuantity * (int) getProduct.get("price"));
                            cart.getItems().add(newCartItem);
                        }
                    }
                }

                updateCartTotals(cart);
                break;

            default:
                throw new IllegalArgumentException("Unsupported coupon type: " + type);
        }

        return cart;
    }

    private void updateCartTotals(Cart cart) {
    	List<CartItem> cartItems = cart.getItems();
        double totalPrice = (cartItems==null || cartItems.isEmpty())? 0.0 : cart.getItems().stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();
        double totalDiscount = (cartItems==null || cartItems.isEmpty())? 0.0 : cart.getItems().stream().mapToDouble(CartItem::getTotalDiscount).sum();
        cart.setTotalPrice(totalPrice);
        cart.setTotalDiscount(totalDiscount);
        cart.setFinalPrice(totalPrice - totalDiscount);
    }
}
