/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.controller;

import com.messagemedia.domainmodels.accounts.VendorAccountId;
import com.messagemedia.numbers.service.client.exception.NumbersServiceException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceForbiddenException;
import com.messagemedia.numbers.service.client.models.*;
import com.messagemedia.restapi.common.accounts.AccountFeatureEnabled;
import com.messagemedia.restapi.common.web.security.SecurityUtils;
import com.messagemedia.restapi.numbers.model.Assignment;
import com.messagemedia.restapi.numbers.model.CreateAssignment;
import com.messagemedia.restapi.numbers.model.Number;
import com.messagemedia.restapi.numbers.model.NumberAssignmentListResponse;
import com.messagemedia.restapi.numbers.model.NumberForward;
import com.messagemedia.restapi.numbers.model.NumbersListResponse;
import com.messagemedia.restapi.numbers.model.UpdateAssignment;
import com.messagemedia.restapi.numbers.service.NumbersService;
import com.messagemedia.service.middleware.security.HubUserFeatureEnabled;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.messagemedia.numbers.service.client.models.NumberAssignmentSearchRequest.NumberAssignmentSearchRequestBuilder;
import static com.messagemedia.service.accountmanagement.client.model.account.feature.AccountFeatureFlag.SELF_SERVE_DEDICATED_NUMBERS;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping(path = "/v1/messaging/numbers/dedicated")
public class NumbersController {

    public static final String NUMBER_PATH = "/{numberId}";
    public static final String NUMBER_ASSIGNMENT_PATH = NUMBER_PATH + "/assignment";
    public static final String NUMBER_ASSIGNMENTS_PATH = "/assignments";
    public static final String NUMBER_FORWARD_PATH = NUMBER_PATH + "/forward";

    // Hub Features
    protected static final String HUB_FEATURE_LIST_NUMBERS = "dedicated-numbers.list";
    protected static final String HUB_FEATURE_GET_NUMBER = "dedicated-numbers.list";
    protected static final String HUB_FEATURE_LIST_ASSIGNMENTS = "management.dedicated-numbers.list";
    protected static final String HUB_FEATURE_GET_ASSIGNMENT = "management.dedicated-numbers.list";
    protected static final String HUB_FEATURE_CREATE_ASSIGNMENT = "management.dedicated-numbers.associate";
    protected static final String HUB_FEATURE_DELETE_ASSIGNMENT = "management.dedicated-numbers.disassociate";
    protected static final String HUB_FEATURE_UPDATE_ASSIGNMENT = "management.dedicated-numbers.update";
    protected static final String SUPPORT_HUB_FEATURE_UPDATE_NUMBER = "support.dedicated-numbers.update";

    private static final String FEATURE_NOT_ENABLED_MESSAGE = "Your account is not enabled to use Self Serve Dedicated Numbers. "
            + "Please contact your MessageMedia account manager for access.";

    private static final String ALL_ACCOUNTS = "all";

    private final NumbersService numbersService;

    @Autowired
    public NumbersController(NumbersService numbersService) {
        this.numbersService = numbersService;
    }


    @SuppressWarnings({"checkstyle:parameterNumber"})
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_LIST_NUMBERS}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<NumbersListResponse> getNumbers(
            HttpServletRequest request,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "matching", required = false) String matching,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "service_types", required = false) ServiceType[] serviceTypes,
            @RequestParam(value = "exact_service_types", required = false) Boolean exactServiceTypes,
            @RequestParam(value = "classification", required = false) Classification classification,
            @RequestParam(value = "token", required = false) UUID token
    ) throws NumbersServiceException {
        NumberSearchRequest numberSearchRequest = NumberSearchRequest.NumberSearchRequestBuilder.aNumberSearchRequestBuilder()
                .withAssigned(false)
                .withAvailableBy(OffsetDateTime.now())
                .withCountry(country)
                .withMatching(matching)
                .withPageSize(pageSize)
                .withServiceTypes(serviceTypes)
                .withExactServiceTypes(exactServiceTypes)
                .withClassification(classification)
                .withToken(token)
                .build();
        return new ResponseEntity<>(numbersService.getNumbers(numberSearchRequest), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_GET_NUMBER}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Number> getNumber(
            HttpServletRequest request,
            @PathVariable UUID numberId
    ) throws NumbersServiceException {
        return new ResponseEntity<>(numbersService.getNumber(numberId), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_PATH, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @HubUserFeatureEnabled(flags = {SUPPORT_HUB_FEATURE_UPDATE_NUMBER}, statusCode = FORBIDDEN)
    public ResponseEntity<Number> updateNumber(
            HttpServletRequest request,
            @PathVariable UUID numberId, @RequestBody @Valid UpdateNumberRequest updateNumberRequest
            ) throws NumbersServiceException {
        return new ResponseEntity<>(numbersService.updateNumber(numberId, updateNumberRequest), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_PATH + "/verification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_LIST_ASSIGNMENTS}, statusCode = FORBIDDEN)
    public ResponseEntity<Number> updateNumberStatus(
            HttpServletRequest request,
            @PathVariable UUID numberId, @RequestBody UpdateNumberRequest updateNumberRequest
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        Assignment assignment = numbersService.getAssignment(numberId);

        if (!vendorAccountId.getVendorId().getVendorId().equals(assignment.getVendorId())
                || (!vendorAccountId.getAccountId().getAccountId().equals(assignment.getAccountId()))) {
            throw new NumbersServiceForbiddenException("The account doesn't have authorization to update status of this number");
        }

        return new ResponseEntity<>(numbersService.updateNumber(numberId, new UpdateNumberRequest(null, null, null, null, Status.PENDING, null)),
                HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_ASSIGNMENTS_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_LIST_ASSIGNMENTS}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    @SuppressWarnings("checkstyle:parameternumber")
    public ResponseEntity<NumberAssignmentListResponse> getNumberAssignments(
            HttpServletRequest request,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "token", required = false) UUID token,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "matching", required = false) String matching,
            @RequestParam(value = "label", required = false) String label,
            @RequestParam(value = "service_types", required = false) ServiceType[] serviceTypes,
            @RequestParam(value = "classification", required = false) Classification classification,
            @RequestParam(value = "status", required = false) Status status
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        NumberAssignmentSearchRequest searchRequest = NumberAssignmentSearchRequestBuilder.aNumberAssignmentSearchRequestBuilder()
                .withVendorId(vendorAccountId.getVendorId().getVendorId())
                .withAccountId(vendorAccountId.getAccountId().getAccountId())
                .withPageSize(pageSize)
                .withToken(token)
                .withServiceTypes(serviceTypes)
                .withCountry(country)
                .withMatching(matching)
                .withLabel(label)
                .withClassification(classification)
                .withStatus(status)
                .build();
        return new ResponseEntity<>(numbersService.getNumberAssignments(searchRequest), HttpStatus.OK);
    }

    @RequestMapping(value = "/accounts" + NUMBER_ASSIGNMENTS_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @HubUserFeatureEnabled(flags = {SUPPORT_HUB_FEATURE_UPDATE_NUMBER}, statusCode = FORBIDDEN)
    @SuppressWarnings("checkstyle:parameternumber")
    public ResponseEntity<NumberAssignmentListResponse> getNumberAssignmentsCrossAccounts(
            HttpServletRequest request,
            @RequestParam(value = "page_size", required = false) Integer pageSize,
            @RequestParam(value = "token", required = false) UUID token,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "label", required = false) String label,
            @RequestParam(value = "service_types", required = false) ServiceType[] serviceTypes,
            @RequestParam(value = "classification", required = false) Classification classification,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "accounts", required = false) String[] accounts,
            @RequestParam(value = "matchings", required = false) String[] matchings
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        NumberAssignmentSearchRequest searchRequest = NumberAssignmentSearchRequestBuilder.aNumberAssignmentSearchRequestBuilder()
                .withVendorId(vendorAccountId.getVendorId().getVendorId())
                .withAccountId(vendorAccountId.getAccountId().getAccountId())
                .withPageSize(pageSize)
                .withToken(token)
                .withServiceTypes(serviceTypes)
                .withCountry(country)
                .withLabel(label)
                .withClassification(classification)
                .withStatus(status)
                .withAccounts(ArrayUtils.isNotEmpty(accounts) ? accounts : new String[]{ALL_ACCOUNTS})
                .withMatchings(matchings)
                .build();
        return new ResponseEntity<>(numbersService.getNumberAssignments(searchRequest), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_ASSIGNMENT_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_GET_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Assignment> getAssignment(
            HttpServletRequest request,
            @PathVariable UUID numberId
    ) throws NumbersServiceException {
        return new ResponseEntity<>(numbersService.getAssignment(numberId), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_ASSIGNMENT_PATH, method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_CREATE_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Assignment> createAssignment(
            HttpServletRequest request,
            @PathVariable UUID numberId, @RequestBody @Valid CreateAssignment createAssignment
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        AssignNumberRequest assignNumberRequest = new AssignNumberRequest(vendorAccountId.getVendorId().getVendorId(),
                vendorAccountId.getAccountId().getAccountId(),
                null,
                createAssignment.getMetadata(),
                createAssignment.getLabel());
        return new ResponseEntity<>(numbersService.createAssignment(numberId, assignNumberRequest), HttpStatus.CREATED);
    }

    @RequestMapping(value = NUMBER_ASSIGNMENT_PATH, method = RequestMethod.PATCH,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_UPDATE_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Assignment> updateAssignment(
            HttpServletRequest request,
            @PathVariable UUID numberId, @RequestBody @Valid UpdateAssignment updateAssignment
    ) throws NumbersServiceException {
        UpdateAssignmentRequest updateAssignmentRequest = new UpdateAssignmentRequest(
                null,
                updateAssignment.getWrappedMetadata(),
                updateAssignment.getWrappedLabel());
        return new ResponseEntity<>(numbersService.updateAssignment(numberId, updateAssignmentRequest), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_ASSIGNMENT_PATH, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_DELETE_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Void> deleteAssignment(
            HttpServletRequest request,
            @PathVariable UUID numberId
    ) throws NumbersServiceException {
        numbersService.deleteAssignment(numberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = NUMBER_FORWARD_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_GET_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<NumberForward> getNumberForward(
            HttpServletRequest request,
            @PathVariable UUID numberId
    ) throws NumbersServiceException {
        return new ResponseEntity<>(numbersService.getNumberForward(numberId), HttpStatus.OK);
    }

    @RequestMapping(value = NUMBER_FORWARD_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_UPDATE_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<NumberForward> createNumberForward(
            HttpServletRequest request,
            @PathVariable UUID numberId,
            @RequestBody @Valid NumberForwardDto numberForwardDto
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        return numbersService.createNumberForwardConfig(numberId,
                numberForwardDto, vendorAccountId);
    }

    @RequestMapping(value = NUMBER_FORWARD_PATH, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @AccountFeatureEnabled(flags = {SELF_SERVE_DEDICATED_NUMBERS}, statusCode = FORBIDDEN, message = FEATURE_NOT_ENABLED_MESSAGE)
    @HubUserFeatureEnabled(flags = {HUB_FEATURE_UPDATE_ASSIGNMENT}, allowIfHeaderIsNotSet = true, statusCode = FORBIDDEN)
    public ResponseEntity<Void> deleteNumberForward(
            HttpServletRequest request,
            @PathVariable UUID numberId
    ) throws NumbersServiceException {
        VendorAccountId vendorAccountId = SecurityUtils.getVendorAccountId();
        numbersService.deleteNumberForward(numberId, vendorAccountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
