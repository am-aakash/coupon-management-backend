package com.example.CouponManagement.util;

public class CouponNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CouponNotFoundException(Long couponId) {
        super("Coupon not found with ID: " + couponId);
    }
}