/*
 * Copyright (c) Message4U Pty Ltd 2014-2021
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.service;

import com.google.common.collect.ImmutableSet;
import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.numbers.service.client.NumbersServiceClient;
import com.messagemedia.numbers.service.client.exception.NumbersServiceBadRequestException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceForbiddenException;
import com.messagemedia.numbers.service.client.models.NumberDto;
import com.messagemedia.numbers.service.client.models.NumberForwardDto;
import com.messagemedia.numbers.service.client.models.ServiceType;
import com.messagemedia.restapi.numbers.model.NumberForward;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.restapi.numbers.TestData.numberDtoWithCallCapability;
import static com.messagemedia.restapi.numbers.TestData.numberForward;
import static com.messagemedia.restapi.numbers.TestData.numberForwardDto;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(MockitoJUnitRunner.class)
public class NumbersServiceNumberForwardTest {

    private static final VendorAccountId VENDOR_ACCOUNT_ID = VendorAccountId.fromColonString(VENDOR_ID + ":" + ACCOUNT_ID);

    @Mock
    private NumbersServiceClient numbersServiceClient;

    private NumbersService numbersService;

    @Before
    public void setup() {
        numbersService = new NumbersService(numbersServiceClient);
    }

    @Test
    public void shouldGetNumberForward() throws Exception {
        NumberForwardDto numberForwardDto = numberForwardDto();
        when(numbersServiceClient.getNumberForward(NUMBER_ID)).thenReturn(numberForwardDto);

        NumberForward expected = numberForward(numberForwardDto);

        NumberForward actual = numbersService.getNumberForward(NUMBER_ID);
        assertReflectionEquals(expected, actual);
    }

    @Test
    public void shouldCreateNumberForward() throws Exception {
        NumberDto numberDto = validNumberDto();
        NumberForwardDto request = numberForwardDto();

        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(numberDto);
        when(numbersServiceClient.createNumberForwardConfig(NUMBER_ID, request))
                .thenReturn(new ResponseEntity<>(request, HttpStatus.CREATED));

        NumberForward expected = numberForward(request);
        NumberForward actual = numbersService.createNumberForwardConfig(NUMBER_ID,
                request, VENDOR_ACCOUNT_ID).getBody();

        assertReflectionEquals(expected, actual);
    }

    @Test(expected = NumbersServiceForbiddenException.class)
    public void shouldCreateNumberForwardNumberNotAssigned() throws Exception {
        NumberDto numberDto = validNumberDto();
        numberDto.getAssignedTo().setAccountId("invalid-account-id");
        numberDto.getAssignedTo().setVendorId("invalid-vendor-id");
        NumberForwardDto request = numberForwardDto();
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(numberDto);
        numbersService.createNumberForwardConfig(NUMBER_ID, request, VendorAccountId.GLOBAL);
    }

    @Test(expected = NumbersServiceForbiddenException.class)
    public void shouldCreateNumberForwardNumberWithoutCallCapability() throws Exception {
        NumberDto numberDto = validNumberDto();
        numberDto.setCapabilities(ImmutableSet.of(ServiceType.SMS));
        NumberForwardDto request = numberForwardDto();
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(numberDto);
        numbersService.createNumberForwardConfig(NUMBER_ID, request, VENDOR_ACCOUNT_ID);
    }

    @Test(expected = NumbersServiceBadRequestException.class)
    public void shouldCreateNumberBadRequestException() throws Exception {
        NumberForwardDto request = new NumberForwardDto("+123456789");
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(validNumberDto());
        when(numbersServiceClient.createNumberForwardConfig(NUMBER_ID, request))
                .thenThrow(new NumbersServiceBadRequestException(null));
        numbersService.createNumberForwardConfig(NUMBER_ID, request, VENDOR_ACCOUNT_ID);
        verify(numbersServiceClient, times(1)).getNumber(NUMBER_ID);
    }

    @Test(expected = NumbersServiceException.class)
    public void shouldCreateNumberForwardException() throws Exception {
        NumberForwardDto request = new NumberForwardDto("+123456789");
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(validNumberDto());
        numbersService.createNumberForwardConfig(NUMBER_ID, request, VendorAccountId.GLOBAL);
        verify(numbersServiceClient, times(1)).getNumber(NUMBER_ID);
    }

    private NumberDto validNumberDto() {
        NumberDto numberDto = numberDtoWithCallCapability();
        numberDto.getAssignedTo().setAccountId(ACCOUNT_ID);
        numberDto.getAssignedTo().setVendorId(VENDOR_ID);
        return numberDto;
    }
}
