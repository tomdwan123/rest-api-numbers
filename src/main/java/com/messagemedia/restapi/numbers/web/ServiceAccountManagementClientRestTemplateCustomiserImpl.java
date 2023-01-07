/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.web;

import com.messagemedia.service.accountmanagement.client.config.ServiceAccountManagementClientRestTemplateCustomiser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class ServiceAccountManagementClientRestTemplateCustomiserImpl implements ServiceAccountManagementClientRestTemplateCustomiser {

    @Override
    public void customise(RestTemplate restTemplate) {
        restTemplate.setInterceptors(Collections.singletonList(new VendorAccountIdInterceptor()));
    }
}
