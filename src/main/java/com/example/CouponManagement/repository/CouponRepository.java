package com.example.CouponManagement.repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.CouponManagement.entity.Coupon;

public class CouponRepository {
    
    private Map<Long, Coupon> coupons = new HashMap<>();
    private long currentId = 1;

    public Coupon addCoupon(Coupon coupon) {
        coupon.setId(currentId++);
        coupons.put(coupon.getId(), coupon);
        return coupon;
    }

    public Optional<Coupon> getCouponById(Long id) {
        return Optional.ofNullable(coupons.get(id));
    }

    public Map<Long, Coupon> getAllCoupons() {
        return new HashMap<>(coupons);
    }

    public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
        updatedCoupon.setId(id);
        coupons.put(id, updatedCoupon);
        return updatedCoupon;
    }

    public void deleteCoupon(Long id) {
        coupons.remove(id);
    }
}
