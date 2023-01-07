/*
 * Copyright (c) Message4U Pty Ltd 2014-2021
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.messagemedia.framework.test.IntegrationTestUtilities.getWebContainerPort;
import static com.messagemedia.framework.test.IntegrationTestUtilities.pathToString;
import static com.messagemedia.restapi.common.accounts.AccountFeatureChecker.ACCOUNT_FEATURES_1;
import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.service.accountmanagement.client.model.account.feature.AccountFeatureFlag.SELF_SERVE_DEDICATED_NUMBERS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RunWith(DataProviderRunner.class)
public class NumbersControllerForwardIT {

    private static final Integer NUMBERS_SERVICE_PORT = 10153;
    private static final String API_SERVICE_ENDPOINT = "http://localhost:" + getWebContainerPort();
    private static final String AUTHENTICATED_ACCOUNT_HEADER = "Authenticated-Account-Id";
    private static final String EFFECTIVE_ACCOUNT_HEADER = "Effective-Account-Id";
    private static final String FEATURE_SWITCH = String.valueOf(SELF_SERVE_DEDICATED_NUMBERS.getBitMask());
    private static final String VENDOR_HEADER = "Vendor-Id";

    private RequestSpecification rest;

    @ClassRule
    public static WireMockRule mockNumbersService = new WireMockRule(NUMBERS_SERVICE_PORT);

    @Before
    public void setup() {
        rest = RestAssured
                .with()
                .urlEncodingEnabled(false)
                .baseUri(API_SERVICE_ENDPOINT)
                .basePath("/v1")
                .log()
                .all();
    }

    @Test
    public void shouldDeleteNumberForward() throws Exception {
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(OK.value())
                                .withBody(pathToString("/numbers-service-number-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        mockNumbersService.stubFor(delete(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/forward"))
                .willReturn(
                        aResponse().withStatus(NO_CONTENT.value())
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        getRest(null, true)
                .when()
                .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
                .then().assertThat()
                .statusCode(NO_CONTENT.value());
    }

    @Test
    public void shouldReturnForbiddenWhenDeletingNumberWithInvalidAssignment() {
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(OK.value())
                                .withBody(pathToString("/numbers-service-invalid-assignment-number-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        getRest(null, true)
            .when()
            .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
            .then().assertThat()
            .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingUnassignedNumber() throws Exception {
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(NOT_FOUND.value())));

        getRest(null, true)
            .when()
            .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
            .then().assertThat()
            .statusCode(NOT_FOUND.value());
    }

    @Test
    public void shouldGetNumberForward() throws Exception {
        // Given
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/forward"))
                .willReturn(
                        aResponse().withStatus(OK.value()).withBody(pathToString("/numbers-service-number-forward-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
                .then().assertThat()
                .statusCode(OK.value())
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-forward-response.json"), response, false);
    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledGetNumberForward() throws Exception {
        getRest(null, false)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldCreateNumberForward() throws Exception {
        // Given
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(OK.value())
                                .withBody(pathToString("/numbers-service-number-with-call-capability-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));
        mockNumbersService.stubFor(post(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/forward"))
                .willReturn(
                        aResponse().withStatus(CREATED.value())
                                .withBody(pathToString("/numbers-service-number-forward-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(pathToString("/number-forward-request.json"), true)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
                .then().assertThat()
                .statusCode(CREATED.value())
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-forward-response.json"), response, false);
    }

    @Test
    public void shouldReturnBadRequestWhenCreateNumberForwardWithInvalidNumber() {
        // Given
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(OK.value())
                                .withBody(pathToString("/numbers-service-number-with-call-capability-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        getRest(pathToString("/number-forward-invalid-request.json"), true)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/forward")
                .then().assertThat()
                .statusCode(BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();
    }

    private RequestSpecification getRest(String body, boolean featureEnabled) {
        RequestSpecification requestSpecification = rest
                                                    .given()
                                                    .header(VENDOR_HEADER, VENDOR_ID)
                                                    .header(AUTHENTICATED_ACCOUNT_HEADER, ACCOUNT_ID)
                                                    .header(EFFECTIVE_ACCOUNT_HEADER, ACCOUNT_ID)
                .response().log().all().request();
        if (featureEnabled) {
            requestSpecification = requestSpecification.header(ACCOUNT_FEATURES_1, FEATURE_SWITCH);
        }
        if (StringUtils.isNotBlank(body)) {
            requestSpecification = requestSpecification.body(body).contentType(ContentType.JSON);
        }
        return requestSpecification;
    }
}
