package com.ecoledger.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void instantiate() {
        OpenApiConfig cfg = new OpenApiConfig();
        assertNotNull(cfg);
    }
}
