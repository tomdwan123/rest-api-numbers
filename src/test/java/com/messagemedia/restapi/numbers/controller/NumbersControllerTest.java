/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.controller;

import com.google.common.collect.ImmutableList;
import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.framework.config.JsonConfig;
import com.messagemedia.numbers.service.client.exception.NumbersServiceBadRequestException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceNotFoundException;
import com.messagemedia.numbers.service.client.models.*;
import com.messagemedia.restapi.common.web.security.SecurityContextTestRule;
import com.messagemedia.restapi.numbers.config.RestNumbersControllerAdvice;
import com.messagemedia.restapi.numbers.model.NumberAssignmentListResponse;
import com.messagemedia.restapi.numbers.model.NumbersListResponse;
import com.messagemedia.restapi.numbers.model.Pagination;
import com.messagemedia.restapi.numbers.service.NumbersService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.UUID;

import static com.messagemedia.framework.test.IntegrationTestUtilities.pathToString;
import static com.messagemedia.restapi.common.accounts.AccountFeatureChecker.ACCOUNT_FEATURES_1;
import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.restapi.numbers.TestData.anotherAssignmentDto;
import static com.messagemedia.restapi.numbers.TestData.anotherNumberAssignmentDto;
import static com.messagemedia.restapi.numbers.TestData.anotherNumberDto;
import static com.messagemedia.restapi.numbers.TestData.assignment;
import static com.messagemedia.restapi.numbers.TestData.assignmentDto;
import static com.messagemedia.restapi.numbers.TestData.number;
import static com.messagemedia.restapi.numbers.TestData.numberAssignment;
import static com.messagemedia.restapi.numbers.TestData.numberAssignmentDto;
import static com.messagemedia.restapi.numbers.TestData.numberDto;
import static com.messagemedia.restapi.numbers.TestData.numberForward;
import static com.messagemedia.restapi.numbers.TestData.numberForwardDto;
import static com.messagemedia.service.accountmanagement.client.model.account.feature.AccountFeatureFlag.SELF_SERVE_DEDICATED_NUMBERS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class NumbersControllerTest {

    public static final String NUMBERS_PATH = "/v1/messaging/numbers/dedicated/";
    private static final String FEATURE_SWITCH = String.valueOf(SELF_SERVE_DEDICATED_NUMBERS.getBitMask());

    @Mock
    private NumbersService numbersService;
    @Captor
    private ArgumentCaptor<NumberSearchRequest> searchRequestCaptor;
    @Captor
    private ArgumentCaptor<NumberAssignmentSearchRequest> assignmentSearchRequestCaptor;
    @Rule
    public SecurityContextTestRule securityContextTestRule = new SecurityContextTestRule();

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = createMockMvc(new NumbersController(numbersService));
        securityContextTestRule.setContext(new VendorAccountId(VENDOR_ID, ACCOUNT_ID));
    }

    @Test
    public void testGetNumbers() throws Exception {
        // Given
        NumbersListResponse response = new NumbersListResponse(
                ImmutableList.of(number(numberDto()), number(anotherNumberDto())),
                new Pagination(50, UUID.fromString("b9ee3fe8-2c20-47b1-96e9-c5d12d7ed985"))
        );
        when(numbersService.getNumbers(any(NumberSearchRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(headers(get(NUMBERS_PATH
                + "?country=AU&matching=abc&page_size=5&service_types=SMS&classification=GOLD&token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/numbers-list-response.json")));

        // Then
        verify(numbersService).getNumbers(searchRequestCaptor.capture());
        NumberSearchRequest searchRequest = searchRequestCaptor.getValue();
        assertEquals("AU", searchRequest.getCountry());
        assertEquals("abc", searchRequest.getMatching());
        assertEquals(5, searchRequest.getPageSize());
        assertArrayEquals(new ServiceType[] {ServiceType.SMS}, searchRequest.getServiceTypes());
        assertEquals(Classification.GOLD, searchRequest.getClassification());
        assertEquals("db4cfd5e-e5fe-4e88-a8b0-9c171f95c891", searchRequest.getToken().toString());
    }

    @Test
    public void testGetNumber() throws Exception {
        when(numbersService.getNumber(NUMBER_ID)).thenReturn(number(numberDto()));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-response.json")));

        verify(numbersService).getNumber(NUMBER_ID);
    }

    @Test
    public void shouldReturn404WhenNotFound() throws Exception {
        when(numbersService.getNumber(NUMBER_ID)).thenThrow(new NumbersServiceNotFoundException("Not found"));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateNumber() throws Exception {
        when(numbersService.updateNumber(eq(NUMBER_ID), any(UpdateNumberRequest.class))).thenReturn(number(numberDto()));

        mockMvc.perform(headers(patch(NUMBERS_PATH + NUMBER_ID))
                        .content(pathToString("/number-update-request.json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-response.json")));

        verify(numbersService).updateNumber(eq(NUMBER_ID), any(UpdateNumberRequest.class));
    }

    @Test
    public void testUpdateNumberStatus() throws Exception {
        when(numbersService.getAssignment(NUMBER_ID)).thenReturn(assignment(assignmentDto()));
        when(numbersService.updateNumber(eq(NUMBER_ID), any(UpdateNumberRequest.class))).thenReturn(number(numberDto()));

        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/verification")).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-response.json")));

        verify(numbersService).getAssignment(NUMBER_ID);
        verify(numbersService).updateNumber(eq(NUMBER_ID), any(UpdateNumberRequest.class));
    }

    @Test
    public void shouldReturn403WhenAccountNotOwnNumber() throws Exception {
        when(numbersService.getAssignment(NUMBER_ID)).thenReturn(assignment(anotherAssignmentDto()));

        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/verification")).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetNumberAssignments() throws Exception {
        // Given
       NumberAssignmentListResponse response = new NumberAssignmentListResponse(
                ImmutableList.of(numberAssignment(numberAssignmentDto()), numberAssignment(anotherNumberAssignmentDto())),
                new Pagination(50, UUID.fromString("b9ee3fe8-2c20-47b1-96e9-c5d12d7ed985"))
        );
        when(numbersService.getNumberAssignments(any(NumberAssignmentSearchRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(headers(get(NUMBERS_PATH + "/assignments"
                + "?country=AU&matching=abc&label=xyz&page_size=5&service_types=SMS&classification=GOLD"
                + "&token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891&status=PENDING")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-assignment-list-response.json")));

        // Then
        verify(numbersService).getNumberAssignments(assignmentSearchRequestCaptor.capture());
        NumberAssignmentSearchRequest searchRequest = assignmentSearchRequestCaptor.getValue();
        assertEquals("AU", searchRequest.getCountry());
        assertEquals("abc", searchRequest.getMatching());
        assertEquals("xyz", searchRequest.getLabel());
        assertEquals(5, searchRequest.getPageSize());
        assertArrayEquals(new ServiceType[] {ServiceType.SMS}, searchRequest.getServiceTypes());
        assertEquals(Classification.GOLD, searchRequest.getClassification());
        assertEquals("db4cfd5e-e5fe-4e88-a8b0-9c171f95c891", searchRequest.getToken().toString());
        assertEquals(Status.PENDING, searchRequest.getStatus());
    }

    @Test
    public void testGetNumberAssignmentsCrossAccounts() throws Exception {
        // Given
        NumberAssignmentListResponse response = new NumberAssignmentListResponse(
                ImmutableList.of(numberAssignment(numberAssignmentDto()), numberAssignment(anotherNumberAssignmentDto())),
                new Pagination(50, UUID.fromString("b9ee3fe8-2c20-47b1-96e9-c5d12d7ed985"))
        );
        when(numbersService.getNumberAssignments(any(NumberAssignmentSearchRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(headers(get(NUMBERS_PATH + "accounts/assignments"
                        + "?country=AU&label=xyz&page_size=5&service_types=SMS&classification=GOLD"
                        + "&token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891&status=PENDING&accounts=all")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-assignment-list-response.json")));

        // Then
        verify(numbersService).getNumberAssignments(assignmentSearchRequestCaptor.capture());
        NumberAssignmentSearchRequest searchRequest = assignmentSearchRequestCaptor.getValue();
        assertEquals("AU", searchRequest.getCountry());
        assertEquals("xyz", searchRequest.getLabel());
        assertEquals(5, searchRequest.getPageSize());
        assertArrayEquals(new ServiceType[] {ServiceType.SMS}, searchRequest.getServiceTypes());
        assertEquals(Classification.GOLD, searchRequest.getClassification());
        assertEquals("db4cfd5e-e5fe-4e88-a8b0-9c171f95c891", searchRequest.getToken().toString());
        assertEquals(Status.PENDING, searchRequest.getStatus());
        assertArrayEquals(new String[]{"all"}, searchRequest.getAccounts());
    }

    @Test
    public void testGetAssignment() throws Exception {
        when(numbersService.getAssignment(NUMBER_ID)).thenReturn(assignment(assignmentDto()));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID + "/assignment")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-assignment-response.json")));

        verify(numbersService).getAssignment(NUMBER_ID);
    }

    @Test
    public void testCreateAssignment() throws Exception {
        when(numbersService.createAssignment(eq(NUMBER_ID), any(AssignNumberRequest.class))).thenReturn(assignment(assignmentDto()));

        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/assignment")
                                .content(pathToString("/number-assignment-request.json"))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-assignment-response.json")));

        verify(numbersService).createAssignment(eq(NUMBER_ID), any(AssignNumberRequest.class));
    }

    @Test
    public void shouldReturn400WhenInvalidRequest() throws Exception {
        when(numbersService.createAssignment(eq(NUMBER_ID), any(AssignNumberRequest.class)))
                .thenThrow(new NumbersServiceBadRequestException("Bad Request"));

        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-request.json"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400WhenInvalidRequestUpdateNumber() throws Exception {
        when(numbersService.updateNumber(eq(NUMBER_ID), any(UpdateNumberRequest.class)))
                .thenThrow(new NumbersServiceBadRequestException("Bad Request"));

        mockMvc.perform(headers(patch(NUMBERS_PATH + NUMBER_ID))
                        .content(pathToString("/number-update-invalid-request.json")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400WhenInvalidMetadata() throws Exception {
        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-invalid-metadata-request.json"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateAssignment() throws Exception {
        when(numbersService.updateAssignment(eq(NUMBER_ID), any(UpdateAssignmentRequest.class))).thenReturn(assignment(assignmentDto()));

        mockMvc.perform(headers(patch(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-request.json"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json(pathToString("/number-assignment-response.json")));

        verify(numbersService).updateAssignment(eq(NUMBER_ID), any(UpdateAssignmentRequest.class));
    }

    @Test
    public void shouldReturn400WhenInvalidMetadataUpdate() throws Exception {
        mockMvc.perform(headers(patch(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-invalid-metadata-request.json"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400WhenCreatingLabelWithMoreThan100Chars() throws Exception {
        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-invalid-100chars-label-request.json"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400WhenUpdatingLabelWithMoreThan100Chars() throws Exception {
        mockMvc.perform(headers(patch(NUMBERS_PATH + NUMBER_ID + "/assignment")
                .content(pathToString("/number-assignment-invalid-100chars-label-request.json"))))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testDeleteAssignment() throws Exception {
        mockMvc.perform(headers(delete(NUMBERS_PATH + NUMBER_ID + "/assignment")))
                .andExpect(status().isNoContent());

        verify(numbersService).deleteAssignment(NUMBER_ID);
    }

    @Test
    public void testGetNumberForward() throws Exception {
        when(numbersService.getNumberForward(NUMBER_ID)).thenReturn(numberForward(new NumberForwardDto("+61491570156")));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID + "/forward")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json(pathToString("/number-forward-response.json")));

        verify(numbersService).getNumberForward(NUMBER_ID);
    }

    @Test
    public void testGetNumberForwardNull() throws Exception {
        when(numbersService.getNumberForward(NUMBER_ID)).thenReturn(numberForward(new NumberForwardDto(null)));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID + "/forward")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json(pathToString("/number-forward-null-response.json")));

        verify(numbersService).getNumberForward(NUMBER_ID);
    }

    @Test
    public void testGetNumberForwardNotFound() throws Exception {
        when(numbersService.getNumberForward(NUMBER_ID)).thenThrow(new NumbersServiceNotFoundException("Not found"));

        mockMvc.perform(headers(get(NUMBERS_PATH + NUMBER_ID + "/forward")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateNumberForward() throws Exception {
        NumberForwardDto numberForwardDto = numberForwardDto();
        when(numbersService.createNumberForwardConfig(
                    any(UUID.class),
                    any(NumberForwardDto.class),
                    any(VendorAccountId.class)))
            .thenReturn(new ResponseEntity<>(numberForward(numberForwardDto), HttpStatus.CREATED));
        mockMvc.perform(headers(post(NUMBERS_PATH + NUMBER_ID + "/forward"))
                    .content(pathToString("/number-forward-request.json")))
            .andExpect(status().isCreated());
    }

    private MockMvc createMockMvc(Object restControllers) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(new JsonConfig().objectMapper());
        StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders
                .standaloneSetup(restControllers)
                .setMessageConverters(messageConverter)
                .setControllerAdvice(new RestNumbersControllerAdvice());
        return standaloneMockMvcBuilder.build();
    }

    private MockHttpServletRequestBuilder headers(MockHttpServletRequestBuilder builder) {
        return builder
                .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                .header(ACCOUNT_FEATURES_1, FEATURE_SWITCH);
    }
}
