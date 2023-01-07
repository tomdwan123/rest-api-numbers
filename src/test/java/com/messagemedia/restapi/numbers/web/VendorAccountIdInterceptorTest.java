/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.web;

import com.messagemedia.domainmodels.accounts.AccountId;
import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.domainmodels.accounts.VendorId;
import com.messagemedia.restapi.common.web.security.SecurityContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import java.lang.reflect.Method;

import static com.messagemedia.domainmodels.accounts.VendorAccountId.GLOBAL;
import static com.messagemedia.service.accountmanagement.client.ServiceAccountManagementClient.HEADER_ON_BEHALF_ACCOUNT_ID;
import static com.messagemedia.service.accountmanagement.client.ServiceAccountManagementClient.HEADER_ON_BEHALF_VENDOR_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VendorAccountIdInterceptorTest {

    @Mock
    private ClientHttpRequestExecution execution;
    @Mock
    private HttpRequest request;

    @Test
    public void shouldSetVendorAndAccountInHeaders() throws Exception {
        // Given
        setVendorAccountIdInContext(GLOBAL);
        VendorAccountIdInterceptor interceptor = new VendorAccountIdInterceptor();
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);

        // When
        interceptor.intercept(request, null, execution);

        // Then
        verify(execution).execute(request, null);
        assertThat(request.getHeaders().get(HEADER_ON_BEHALF_VENDOR_ID).get(0), is(VendorId.GLOBAL.getVendorId()));
        assertThat(request.getHeaders().get(HEADER_ON_BEHALF_ACCOUNT_ID).get(0), is(AccountId.GLOBAL.getAccountId()));
    }

    private void setVendorAccountIdInContext(VendorAccountId vendorAccountId) throws Exception {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getVendorAccountId()).thenReturn(vendorAccountId);
        Class<?> clazz = Class.forName("com.messagemedia.restapi.common.web.security.SecurityContextHolder");
        Method method = clazz.getMethod("setContext", SecurityContext.class);
        method.setAccessible(true);
        method.invoke(null, securityContext);
    }
}
