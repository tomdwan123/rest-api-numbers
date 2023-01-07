/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.web;

import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.restapi.common.web.security.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static com.messagemedia.service.accountmanagement.client.ServiceAccountManagementClient.HEADER_ON_BEHALF_ACCOUNT_ID;
import static com.messagemedia.service.accountmanagement.client.ServiceAccountManagementClient.HEADER_ON_BEHALF_VENDOR_ID;

public class VendorAccountIdInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        HttpHeaders headers = request.getHeaders();
        headers.add(HEADER_ON_BEHALF_VENDOR_ID, vendorAccountId.getVendorId().getVendorId());
        headers.add(HEADER_ON_BEHALF_ACCOUNT_ID, vendorAccountId.getAccountId().getAccountId());
        return execution.execute(request, body);
    }
}
