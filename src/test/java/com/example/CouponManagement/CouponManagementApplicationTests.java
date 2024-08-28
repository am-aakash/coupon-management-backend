package com.example.CouponManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.CouponManagement.entity.Cart;
import com.example.CouponManagement.entity.CartItem;
import com.example.CouponManagement.entity.Coupon;
import com.example.CouponManagement.repository.CouponRepository;
import com.example.CouponManagement.service.CouponService;
import com.example.CouponManagement.util.CouponExpiredException;
import com.example.CouponManagement.util.CouponNotFoundException;

@SpringBootTest
class CouponManagementApplicationTests {

	@Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setType("cart-wise");
        coupon.setDetails(Map.of("threshold", 100, "discount", 10));
        coupon.setExpirationDate(LocalDate.now().plusDays(1));

        when(couponRepository.addCoupon(any(Coupon.class))).thenReturn(coupon);

        Coupon createdCoupon = couponService.createCoupon(coupon);
        assertNotNull(createdCoupon);
        assertEquals(1L, createdCoupon.getId());
        assertEquals("cart-wise", createdCoupon.getType());
    }

    @Test
    public void testApplyCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setType("cart-wise");
        coupon.setDetails(Map.of("threshold", 100, "discount", 10));
        coupon.setExpirationDate(LocalDate.now().plusDays(1));

        Cart cart = new Cart();
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(1L, 6, 50));
        items.add(new CartItem(2L, 3, 30));
        items.add(new CartItem(3L, 2, 25));
        cart.setItems(items);

        when(couponRepository.getCouponById(anyLong())).thenReturn(Optional.of(coupon));

        Cart updatedCart = couponService.applyCoupon(1L, cart);
        assertNotNull(updatedCart);
        assertEquals(440.0, updatedCart.getTotalPrice());
        assertEquals(44.0, updatedCart.getTotalDiscount());
        assertEquals(396.0, updatedCart.getFinalPrice());
    }

    @Test
    public void testApplyCoupon_CouponNotFound() {
        when(couponRepository.getCouponById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(CouponNotFoundException.class, () -> {
            couponService.applyCoupon(1L, new Cart());
        });

        assertEquals("Coupon not found with ID: Coupon with ID 1 not found.", exception.getMessage());
    }

    @Test
    public void testApplyCoupon_CouponExpired() {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setType("cart-wise");
        coupon.setDetails(Map.of("threshold", 100, "discount", 10));
        coupon.setExpirationDate(LocalDate.now().minusDays(1));

        when(couponRepository.getCouponById(anyLong())).thenReturn(Optional.of(coupon));

        Exception exception = assertThrows(CouponExpiredException.class, () -> {
            couponService.applyCoupon(1L, new Cart());
        });

        assertEquals("Coupon with ID 1 is expired.", exception.getMessage());
    }
}
