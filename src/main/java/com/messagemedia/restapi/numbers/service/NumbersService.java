/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.service;

import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.numbers.service.client.NumbersServiceClient;
import com.messagemedia.numbers.service.client.exception.NumbersServiceException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceForbiddenException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceNotFoundException;
import com.messagemedia.numbers.service.client.models.AssignNumberRequest;
import com.messagemedia.numbers.service.client.models.AssignmentDto;
import com.messagemedia.numbers.service.client.models.NumberAssignmentDto;
import com.messagemedia.numbers.service.client.models.NumberAssignmentSearchRequest;
import com.messagemedia.numbers.service.client.models.NumberDto;
import com.messagemedia.numbers.service.client.models.NumberForwardDto;
import com.messagemedia.numbers.service.client.models.NumberListResponse;
import com.messagemedia.numbers.service.client.models.NumberSearchRequest;
import com.messagemedia.numbers.service.client.models.PageMetadata;
import com.messagemedia.numbers.service.client.models.UpdateAssignmentRequest;
import com.messagemedia.numbers.service.client.models.UpdateNumberRequest;
import com.messagemedia.restapi.numbers.model.Assignment;
import com.messagemedia.restapi.numbers.model.Number;
import com.messagemedia.restapi.numbers.model.NumberAssignment;
import com.messagemedia.restapi.numbers.model.NumberAssignmentListResponse;
import com.messagemedia.restapi.numbers.model.NumberForward;
import com.messagemedia.restapi.numbers.model.NumbersListResponse;
import com.messagemedia.restapi.numbers.model.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.messagemedia.numbers.service.client.models.ServiceType.CALL;

@Service
public class NumbersService {

    private final NumbersServiceClient numbersServiceClient;

    @Autowired
    public NumbersService(NumbersServiceClient numbersServiceClient) {
        this.numbersServiceClient = numbersServiceClient;
    }

    public NumbersListResponse getNumbers(NumberSearchRequest numberSearchRequest) throws NumbersServiceException {
        return convert(numbersServiceClient.getNumbers(numberSearchRequest));
    }

    public Number getNumber(UUID numberId) throws NumbersServiceException {
        return convert(numbersServiceClient.getNumber(numberId));
    }

    public Number updateNumber(UUID numberId, UpdateNumberRequest updateNumberRequest) throws NumbersServiceException {
        return convert(numbersServiceClient.updateNumber(numberId, updateNumberRequest));
    }

    public NumberForward getNumberForward(UUID numberId) throws NumbersServiceException {
        return convert(numbersServiceClient.getNumberForward(numberId));
    }

    public NumberAssignmentListResponse getNumberAssignments(NumberAssignmentSearchRequest searchRequest) throws NumbersServiceException {
        return convert(numbersServiceClient.getAssignments(searchRequest));
    }

    public Assignment getAssignment(UUID numberId) throws NumbersServiceException {
        return convert(numbersServiceClient.getAssignment(numberId));
    }

    public Assignment createAssignment(UUID numberId, AssignNumberRequest request) throws NumbersServiceException {
        return convert(numbersServiceClient.createAssignment(numberId, request));
    }

    public Assignment updateAssignment(UUID numberId, UpdateAssignmentRequest request) throws NumbersServiceException {
        return convert(numbersServiceClient.updateAssignment(numberId, request));
    }

    public void deleteAssignment(UUID numberId) throws NumbersServiceException {
        numbersServiceClient.deleteAssignment(numberId);
    }

    public ResponseEntity<NumberForward> createNumberForwardConfig(
            UUID numberId,
            NumberForwardDto request,
            VendorAccountId vendorAccountId
    ) throws NumbersServiceException {
        NumberDto number = numbersServiceClient.getNumber(numberId);

        // Check if number has `CALL` capability
        if (number.getCapabilities() == null || !number.getCapabilities().contains(CALL)) {
            throw new NumbersServiceForbiddenException(String.format("Number does not have call capability: %s",
                    number.getId()));
        }

        // Check if number is not assigned
        if (!isNumberAssigned(number, vendorAccountId)) {
            String errorMsg = String.format("Number is not assigned to vendor/account: %s", vendorAccountId.toColonString());

            if (number.getAssignedTo() == null) {
                throw new NumbersServiceNotFoundException(errorMsg);
            }

            throw new NumbersServiceForbiddenException(errorMsg);
        }

        return createNumberForwardConfigResponse(numbersServiceClient.createNumberForwardConfig(numberId, request));
    }

    private ResponseEntity<NumberForward> createNumberForwardConfigResponse(
            ResponseEntity<NumberForwardDto> response
    ) {
        return new ResponseEntity<>(
                convert(Objects.requireNonNull(response.getBody())),
                response.getStatusCode() == HttpStatus.OK
                        ? HttpStatus.OK
                        : HttpStatus.CREATED
        );
    }

    public void deleteNumberForward(UUID numberId, VendorAccountId vendorAccountId) throws NumbersServiceException {
        NumberDto numberDto = numbersServiceClient.getNumber(numberId);
        String assignmentErrorMsg = String.format("Number is not assigned to vendor/account: %s / %s",
                vendorAccountId.getVendorId().toString(),
                vendorAccountId.getAccountId().toString());

        // Check if number is assigned
        if (numberDto.getAssignedTo() == null) {
            throw new NumbersServiceNotFoundException(assignmentErrorMsg);
        }

        // Check if number is assigned to correct vendor/account.
        if (!vendorAccountId.getAccountId().toString().equals(numberDto.getAssignedTo().getAccountId())
            || !vendorAccountId.getVendorId().toString().equals(numberDto.getAssignedTo().getVendorId())) {
            throw new NumbersServiceForbiddenException(assignmentErrorMsg);
        }

        numbersServiceClient.deleteNumberForward(numberId);
    }

    private NumbersListResponse convert(NumberListResponse numberListResponse) {
        return new NumbersListResponse(
                numberListResponse.getNumbers().stream().map(this::convert).collect(Collectors.toList()),
                convert(numberListResponse.getPageMetadata())
        );
    }

    private NumberAssignmentListResponse convert(com.messagemedia.numbers.service.client.models.NumberAssignmentListResponse assignmentListResponse) {
        return new NumberAssignmentListResponse(
                assignmentListResponse.getNumberAssignments().stream().map(this::convert).collect(Collectors.toList()),
                convert(assignmentListResponse.getPageMetadata())
        );
    }

    private NumberAssignment convert(NumberAssignmentDto numberAssignmentDto) {
        return new NumberAssignment(convert(numberAssignmentDto.getNumber()), convert(numberAssignmentDto.getAssignment()));
    }

    private Number convert(NumberDto numberDto) {
        return new Number(numberDto.getId(),
                numberDto.getPhoneNumber(),
                numberDto.getCountry(),
                numberDto.getType(),
                numberDto.getClassification(),
                numberDto.getAvailableAfter(),
                numberDto.getCapabilities(),
                numberDto.getStatus()
        );
    }

    private Assignment convert(AssignmentDto assignmentDto) {
        return new Assignment(
                assignmentDto.getMetadata(),
                assignmentDto.getId(),
                assignmentDto.getNumberId(),
                assignmentDto.getLabel(),
                assignmentDto.getAccountId(),
                assignmentDto.getVendorId()
        );
    }

    private Pagination convert(PageMetadata metadata) {
        Pagination pagination = new Pagination();
        if (null != metadata) {
            pagination.setPageSize(metadata.getPageSize());
            pagination.setNextToken(metadata.getToken());
        }
        return pagination;
    }

    private NumberForward convert(NumberForwardDto numberForwardDto) {
        return new NumberForward(
                numberForwardDto.getDestination()
        );
    }

    private boolean isNumberAssigned(NumberDto number, VendorAccountId vendorAccountId) {
        return (number.getAssignedTo() != null
                && vendorAccountId.getAccountId().toString().equals(number.getAssignedTo().getAccountId())
                && vendorAccountId.getVendorId().toString().equals(number.getAssignedTo().getVendorId()));
    }
}
