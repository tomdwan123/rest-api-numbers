/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.controller;

import com.messagemedia.restapi.common.pagination.CollectionDto;
import com.messagemedia.restapi.common.web.security.SecurityUtils;
import com.messagemedia.restapi.numbers.model.RotaryNumber;
import com.messagemedia.restapi.numbers.service.RotaryNumbersService;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/messaging/numbers/rotary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RotaryNumbersController {

    private final RotaryNumbersService rotaryNumbersService;

    @Autowired
    public RotaryNumbersController(RotaryNumbersService rotaryNumbersService) {
        this.rotaryNumbersService = rotaryNumbersService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public CollectionDto<RotaryNumber> getRotaryNumbers() throws ServiceAccountManagementException {
        return new CollectionDto<>(null, rotaryNumbersService.getRotaryNumbers(SecurityUtils.getVendorAccountId().getAccountId()));
    }
}
