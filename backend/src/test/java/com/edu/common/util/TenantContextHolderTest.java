package com.edu.common.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Test
    void testTenantIdPropagationToChildThread() throws Exception {
        TenantContextHolder.setTenantId(42L);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Long propagatedId = CompletableFuture
                    .supplyAsync(() -> TenantContextHolder.getTenantId(), executor)
                    .get(5, TimeUnit.SECONDS);
            assertEquals(42L, propagatedId);
        } finally {
            executor.shutdown();
            TenantContextHolder.clear();
        }
    }
}
