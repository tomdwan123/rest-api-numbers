/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.service;

import com.google.common.collect.ImmutableList;
import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.framework.jackson.core.valuewithnull.ValueWithNull;
import com.messagemedia.numbers.service.client.NumbersServiceClient;
import com.messagemedia.numbers.service.client.models.AssignNumberRequest;
import com.messagemedia.numbers.service.client.models.NumberAssignmentSearchRequest;
import com.messagemedia.numbers.service.client.models.NumberSearchRequest;
import com.messagemedia.numbers.service.client.models.UpdateAssignmentRequest;
import com.messagemedia.restapi.numbers.model.Assignment;
import com.messagemedia.restapi.numbers.model.Number;
import com.messagemedia.restapi.numbers.model.NumberAssignmentListResponse;
import com.messagemedia.restapi.numbers.model.NumbersListResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.messagemedia.numbers.service.client.models.NumberAssignmentSearchRequest.NumberAssignmentSearchRequestBuilder;
import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.ASSIGNMENT_METADATA;
import static com.messagemedia.restapi.numbers.TestData.ASSIGNMENT_LABEL;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.restapi.numbers.TestData.anotherNumberAssignmentDto;
import static com.messagemedia.restapi.numbers.TestData.anotherNumberDto;
import static com.messagemedia.restapi.numbers.TestData.assignment;
import static com.messagemedia.restapi.numbers.TestData.assignmentDto;
import static com.messagemedia.restapi.numbers.TestData.number;
import static com.messagemedia.restapi.numbers.TestData.numberAssignment;
import static com.messagemedia.restapi.numbers.TestData.numberAssignmentDto;
import static com.messagemedia.restapi.numbers.TestData.numberAssignmentListResponse;
import static com.messagemedia.restapi.numbers.TestData.numberDto;
import static com.messagemedia.restapi.numbers.TestData.numberListResponse;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(MockitoJUnitRunner.class)
public class NumbersServiceTest {

    @Mock
    private NumbersServiceClient numbersServiceClient;

    private NumbersService numbersService;

    @Before
    public void setup() {
        numbersService = new NumbersService(numbersServiceClient);
    }

    @Test
    public void shouldFetchNumbers() throws Exception {
        NumberSearchRequest request = NumberSearchRequest.NumberSearchRequestBuilder.aNumberSearchRequestBuilder()
                                        .withCountry("AU")
                                        .build();
        when(numbersServiceClient.getNumbers(request)).thenReturn(numberListResponse());

        NumbersListResponse actual = numbersService.getNumbers(request);
        assertReflectionEquals(actual.getData(), ImmutableList.of(number(numberDto()), number(anotherNumberDto())));
    }

    @Test
    public void shouldFetchNumber() throws Exception {
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(numberDto());

        Number expected = number(numberDto());

        Number actual = numbersService.getNumber(NUMBER_ID);
        assertReflectionEquals(expected, actual);
    }

    @Test
    public void shouldFetchNumberAssignments() throws Exception {
        NumberAssignmentSearchRequest request = NumberAssignmentSearchRequestBuilder.aNumberAssignmentSearchRequestBuilder()
                .withVendorId(VENDOR_ID)
                .withAccountId(ACCOUNT_ID)
                .build();

        when(numbersServiceClient.getAssignments(request)).thenReturn(numberAssignmentListResponse());

        NumberAssignmentListResponse actual = numbersService.getNumberAssignments(request);
        assertReflectionEquals(actual.getData(),
                ImmutableList.of(numberAssignment(numberAssignmentDto()), numberAssignment(anotherNumberAssignmentDto())));
    }

    @Test
    public void shouldFetchAssignment() throws Exception {
        when(numbersServiceClient.getAssignment(NUMBER_ID)).thenReturn(assignmentDto());

        Assignment expected = assignment(assignmentDto());

        Assignment actual = numbersService.getAssignment(NUMBER_ID);
        assertReflectionEquals(expected, actual);
    }

    @Test
    public void shouldCreateAssignment() throws Exception {
        AssignNumberRequest request = new AssignNumberRequest(VENDOR_ID, ACCOUNT_ID, null, ASSIGNMENT_METADATA, ASSIGNMENT_LABEL);
        when(numbersServiceClient.createAssignment(NUMBER_ID, request)).thenReturn(assignmentDto());

        Assignment expected = assignment(assignmentDto());

        Assignment actual = numbersService.createAssignment(NUMBER_ID, request);
        assertReflectionEquals(expected, actual);
    }

    @Test
    public void shouldUpdateAssignment() throws Exception {
        UpdateAssignmentRequest request =
                new UpdateAssignmentRequest(null, ValueWithNull.of(ASSIGNMENT_METADATA), ValueWithNull.of(ASSIGNMENT_LABEL));
        when(numbersServiceClient.updateAssignment(NUMBER_ID, request)).thenReturn(assignmentDto());

        Assignment expected = assignment(assignmentDto());

        Assignment actual = numbersService.updateAssignment(NUMBER_ID, request);
        assertReflectionEquals(expected, actual);
    }

    @Test
    public void shouldDeleteAssignment() throws Exception {
        numbersService.deleteAssignment(NUMBER_ID);
    }

    @Test
    public void shouldDeleteNumberForward() throws Exception {
        VendorAccountId vendorAccountId = VendorAccountId.fromColonString(VENDOR_ID + ":" + ACCOUNT_ID);
        when(numbersServiceClient.getNumber(NUMBER_ID)).thenReturn(numberDto());
        numbersService.deleteNumberForward(NUMBER_ID, vendorAccountId);
    }
}
