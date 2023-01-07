/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.web;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

public class ServiceAccountManagementClientRestTemplateCustomiserImplTest {

    @Test
    public void shouldSetInterceptors() {
        // Given
        ServiceAccountManagementClientRestTemplateCustomiserImpl customiser = new ServiceAccountManagementClientRestTemplateCustomiserImpl();
        RestTemplate restTemplate = new RestTemplate();

        // When
        customiser.customise(restTemplate);

        // Then
        assertThat(restTemplate.getInterceptors(), hasSize(1));
        assertTrue(restTemplate.getInterceptors().get(0) instanceof VendorAccountIdInterceptor);
    }
}
