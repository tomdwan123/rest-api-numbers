/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.controller;

import com.messagemedia.domainmodels.accounts.AccountId;
import com.messagemedia.restapi.common.web.config.RestApiWorkerInitializer;
import com.messagemedia.restapi.common.web.security.VendorAccountIdFilter;
import com.messagemedia.restapi.numbers.config.RestNumbersControllerAdvice;
import com.messagemedia.restapi.numbers.model.RotaryNumber;
import com.messagemedia.restapi.numbers.service.RotaryNumbersService;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementNotFoundException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class RotaryNumbersControllerTest {

    private static final AccountId ACCOUNT_ID = new AccountId("FunGuys007");
    private static final String VENDOR = "MessageMedia";

    @Mock
    private RotaryNumbersService rotaryNumbersService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = createMockMvc(new RotaryNumbersController(rotaryNumbersService));
    }

    @Test
    public void testGetRotaryNumbers() throws Exception {
        // Given
        when(rotaryNumbersService.getRotaryNumbers(ACCOUNT_ID))
                .thenReturn(newHashSet(new RotaryNumber("+61412345888"), new RotaryNumber("+61412345777"), new RotaryNumber("+61412345444")));

        // When
        mockMvc.perform(authHeaders(get("/v1/messaging/numbers/rotary")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("/rotary_numbers.json"))));

        // Then
        verify(rotaryNumbersService).getRotaryNumbers(ACCOUNT_ID);
    }

    @Test
    public void shouldReturn404WhenAmsNotFound() throws Exception {
        // Given
        when(rotaryNumbersService.getRotaryNumbers(ACCOUNT_ID)).thenThrow(new ServiceAccountManagementNotFoundException("Not found"));

        // When / then
        mockMvc.perform(authHeaders(get("/v1/messaging/numbers/rotary")))
                .andExpect(status().isNotFound());
    }

    private MockMvc createMockMvc(Object restControllers) {
        StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders
                .standaloneSetup(restControllers)
                .setControllerAdvice(new RestNumbersControllerAdvice())
                .addFilter(new VendorAccountIdFilter(RestApiWorkerInitializer.EXCLUDED_PATHS));
        return standaloneMockMvcBuilder.build();
    }

    private MockHttpServletRequestBuilder authHeaders(MockHttpServletRequestBuilder builder) {
        return builder
                .header("Authenticated-Account-Id", ACCOUNT_ID)
                .header("Effective-Account-Id", ACCOUNT_ID)
                .header("Vendor-Id", VENDOR);
    }
}
