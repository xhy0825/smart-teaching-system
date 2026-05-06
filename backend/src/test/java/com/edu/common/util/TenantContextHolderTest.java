package com.edu.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

    @Test
    void testSetAndGetTenantId() {
        TenantContextHolder.setTenantId(1L);
        assertEquals(1L, TenantContextHolder.getTenantId());
        TenantContextHolder.clear();
    }

    @Test
    void testClear() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    void testDefaultIsNull() {
        assertNull(TenantContextHolder.getTenantId());
    }
}
