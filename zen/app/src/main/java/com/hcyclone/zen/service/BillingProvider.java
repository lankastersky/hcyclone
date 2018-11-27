package com.hcyclone.zen.service;

/** An interface that provides an access to BillingLibrary methods. */
public interface BillingProvider {
    BillingService getBillingService();
    boolean isPremiumPurchased();
}

