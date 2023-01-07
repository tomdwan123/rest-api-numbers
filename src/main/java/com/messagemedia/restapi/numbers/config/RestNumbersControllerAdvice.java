/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.messagemedia.numbers.service.client.exception.NumbersServiceBadRequestException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceForbiddenException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceNotFoundException;
import com.messagemedia.numbers.service.client.exception.NumbersServiceTimeoutException;
import com.messagemedia.restapi.common.accounts.AccountFeatureException;
import com.messagemedia.restapi.common.web.StandardRestControllerError;
import com.messagemedia.restapi.common.web.controller.DefaultExceptionHandlerControllerAdvice;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementBadRequestException;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementConflictException;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementException;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementForbiddenException;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementNotFoundException;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementTimeoutException;
import com.messagemedia.service.middleware.security.exception.FeatureSetNotFoundException;
import com.messagemedia.service.middleware.security.exception.UserFeatureException;
import com.messagemedia.framework.logging.Logger;
import com.messagemedia.framework.logging.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * This class ensures that any exceptions
 * are logged in a format that is useful but not overly verbose. It overrides
 * methods in the BaseExceptionHandler class to produce ResponseEntity instances
 * containing additional error information.
 */
@ControllerAdvice
public class RestNumbersControllerAdvice extends DefaultExceptionHandlerControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNumbersControllerAdvice.class);
    private static final StandardRestControllerError NOT_FOUND_REST_ERROR = new StandardRestControllerError("Not found");

    /**
     * Construct a ResponseEntity that represents an Bad Request response, and
     * provides a response containing the list of validation errors.
     * <p>
     * This method can be overridden by subclasses to provide more detailed
     * error responses.
     *
     * @param errorList list of validation errors to include in the output
     * @return a new instance of ResponseEntity
     */
    @Override
    protected ResponseEntity<StandardRestControllerError> makeBadRequestResponse(List<String> errorList) {
        return new ResponseEntity<>(new StandardRestControllerError("Request failed to parse correctly. Please ensure input is valid and try again.",
                                                                    errorList), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceAccountManagementException.class)
    public ResponseEntity<StandardRestControllerError> handleServiceAccountManagementException(ServiceAccountManagementException e) {
        return getStandardRestControllerErrorResponseEntity(e.getCause());
    }

    @ExceptionHandler(ServiceAccountManagementNotFoundException.class)
    public ResponseEntity<StandardRestControllerError> handleServiceAccountManagementNotFoundException(
            ServiceAccountManagementNotFoundException e) {
        return new ResponseEntity<>(NOT_FOUND_REST_ERROR, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceAccountManagementForbiddenException.class)
    public ResponseEntity<StandardRestControllerError> handleServiceAccountManagementForbiddenException(
            ServiceAccountManagementForbiddenException e) {
        return new ResponseEntity<>(new StandardRestControllerError("Account not found"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceAccountManagementConflictException.class)
    public ResponseEntity<StandardRestControllerError> handleServiceAccountManagementConflictException(
            ServiceAccountManagementConflictException e) {
        return new ResponseEntity<>(new StandardRestControllerError("Conflict"), HttpStatus.CONFLICT);
    }

    @ResponseBody
    @ResponseStatus(GATEWAY_TIMEOUT)
    @ExceptionHandler(ServiceAccountManagementTimeoutException.class)
    public StandardRestControllerError handleAMSiTimeoutException(ServiceAccountManagementTimeoutException e) {
        LOGGER.errorWithReason("Request timed out: [{}]", e.getMessage());
        return new StandardRestControllerError("Request to get rotary numbers timed out, please try again later");
    }

    @ExceptionHandler(ServiceAccountManagementBadRequestException.class)
    public ResponseEntity<StandardRestControllerError> handleServiceAccountManagementBadRequestException(
            ServiceAccountManagementBadRequestException e) {
        List<String> errorList = e.getErrorInformation().isPresent() ? singletonList(e.getErrorInformation().get().getMessage()) : newArrayList();
        return makeBadRequestResponse(errorList);
    }

    @ResponseBody
    @ResponseStatus(GATEWAY_TIMEOUT)
    @ExceptionHandler(NumbersServiceTimeoutException.class)
    public StandardRestControllerError handleNumbersServiceiTimeoutException(NumbersServiceTimeoutException e) {
        LOGGER.errorWithReason("Request timed out: [{}]", e.getMessage());
        return new StandardRestControllerError("Request to numbers service timed out, please try again later");
    }

    @ExceptionHandler(NumbersServiceBadRequestException.class)
    public ResponseEntity<StandardRestControllerError> handleNumbersServiceBadRequestException(
            NumbersServiceBadRequestException e) {
        List<String> errorList = e.getErrorInformation().isPresent() ? singletonList(e.getErrorInformation().get().getMessage()) : newArrayList();
        return makeBadRequestResponse(errorList);
    }

    @ExceptionHandler(NumbersServiceNotFoundException.class)
    public ResponseEntity<StandardRestControllerError> handleNumbersServiceNotFoundException(
            NumbersServiceNotFoundException e) {
        return new ResponseEntity<>(NOT_FOUND_REST_ERROR, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NumbersServiceException.class)
    public ResponseEntity<StandardRestControllerError> handleNumbersServiceException(NumbersServiceException e) {
        return getStandardRestControllerErrorResponseEntity(e.getCause());
    }

    @ExceptionHandler(value = {UserFeatureException.class})
    public ResponseEntity<StandardRestControllerError> handleAccountFeatureExceptions(UserFeatureException exception) {
        LOGGER.warn("Hub Feature not enabled error [{}]", exception.getMessage());
        return new ResponseEntity<>(new StandardRestControllerError(exception.getMessage()), exception.getStatus());
    }

    @ExceptionHandler(value = {FeatureSetNotFoundException.class})
    public ResponseEntity<StandardRestControllerError> handleAccountFeatureExceptions(FeatureSetNotFoundException exception) {
        LOGGER.warn("Hub Feature not found error [{}]", exception.getMessage());
        return new ResponseEntity<>(new StandardRestControllerError(exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {AccountFeatureException.class})
    public ResponseEntity<StandardRestControllerError> handleAccountFeatureExceptions(AccountFeatureException exception) {
        LOGGER.warn("Feature not enabled error [{}]", exception.getMessage());
        return new ResponseEntity<>(new StandardRestControllerError(exception.getMessage()), exception.getStatus());
    }

    @ExceptionHandler(value = {NumbersServiceForbiddenException.class})
    public ResponseEntity<StandardRestControllerError> handleNumbersServiceForbiddenException(NumbersServiceForbiddenException exception) {
        LOGGER.warn("Forbidden {}", exception.getMessage());
        return new ResponseEntity<>(new StandardRestControllerError(exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<StandardRestControllerError> getStandardRestControllerErrorResponseEntity(Throwable cause) {
        if (cause instanceof HttpClientErrorException) {
            HttpClientErrorException clientErrorException = (HttpClientErrorException) cause;
            return new ResponseEntity<>(new StandardRestControllerError(clientErrorException.getStatusText()), clientErrorException.getStatusCode());
        } else {
            return new ResponseEntity<>(new StandardRestControllerError("Something went wrong"), INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @VisibleForTesting
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }
}
