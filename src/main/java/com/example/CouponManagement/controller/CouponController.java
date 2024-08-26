package com.example.CouponManagement.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CouponManagement.entity.Cart;
import com.example.CouponManagement.entity.Coupon;
import com.example.CouponManagement.service.CouponService;

@RestController
@RequestMapping("/")
public class CouponController {

    private CouponService couponService = new CouponService();

    @PostMapping("coupons")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.createCoupon(coupon);
    }

    @GetMapping("coupons")
    public Map<Long, Coupon> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @GetMapping("coupons/{id}")
    public Optional<Coupon> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id);
    }

    @PutMapping("coupons/{id}")
    public Coupon updateCoupon(@PathVariable Long id, @RequestBody Coupon updatedCoupon) {
        return couponService.updateCoupon(id, updatedCoupon);
    }

    @DeleteMapping("coupons/{id}")
    public void deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
    }

    @PostMapping("applicable-coupons")
    public List<Coupon> getApplicableCoupons(@RequestBody Map<String, Cart> requestBody) {
        Cart cart = requestBody.get("cart");
        if (cart == null || cart.getItems() == null) {
            throw new IllegalArgumentException("Cart cannot be null or empty.");
        }
        return couponService.getApplicableCoupons(cart);
    }

    @PostMapping("apply-coupon/{id}")
    public Cart applyCoupon(@PathVariable Long id, @RequestBody Map<String, Cart> requestBody) {
        Cart cart = requestBody.get("cart");
        if (cart == null || cart.getItems() == null) {
            throw new IllegalArgumentException("Cart cannot be null or empty.");
        }
        return couponService.applyCoupon(id, cart);
    }
}
