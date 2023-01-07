/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.messagemedia.framework.test.IntegrationTestUtilities.getWebContainerPort;
import static com.messagemedia.framework.test.IntegrationTestUtilities.pathToString;
import static com.messagemedia.restapi.common.accounts.AccountFeatureChecker.ACCOUNT_FEATURES_1;
import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.service.accountmanagement.client.model.account.feature.AccountFeatureFlag.SELF_SERVE_DEDICATED_NUMBERS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RunWith(DataProviderRunner.class)
public class NumbersControllerIT {

    private static final String VENDOR_HEADER = "Vendor-Id";
    private static final String EFFECTIVE_ACCOUNT_HEADER = "Effective-Account-Id";
    private static final String AUTHENTICATED_ACCOUNT_HEADER = "Authenticated-Account-Id";

    private static final String API_SERVICE_ENDPOINT = "http://localhost:" + getWebContainerPort();
    private static final Integer NUMBERS_SERVICE_PORT = 10153;

    private static final String FEATURE_SWITCH = String.valueOf(SELF_SERVE_DEDICATED_NUMBERS.getBitMask());

    @ClassRule
    public static WireMockRule mockNumbersService = new WireMockRule(NUMBERS_SERVICE_PORT);

    private RequestSpecification rest;

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
    @DataProvider({
            "country=AU,AU,,,,,,50",
            "token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891,,db4cfd5e-e5fe-4e88-a8b0-9c171f95c891,,,,,50",
            "service_types=SMS,,,SMS,,,,50",
            "service_types=SMS&exact_service_types=false,,,SMS,false,,,50",
            "matching=abc,,,,,abc,,50",
            "classification=SILVER,,,,,,SILVER,50",
            "page_size=10,,,,,,,10",
            "country=AU&matching=abc&page_size=5&service_types=SMS&exact_service_types=false&classification=GOLD&"
                    + "token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891,AU,db4cfd5e-e5fe-4e88-a8b0-9c171f95c891,SMS,false,abc,GOLD,5",
    })
    public void shouldGetDedicatedNumbers(String queryString, String expectedCountry, String expectedToken, String expectedServiceTypes,
                                          String expectedExactServiceTypes,
                                          String expectedMatching, String expectedClassification, String expectedPageSize) throws Exception {
        // Given
        mockNumbersService.stubFor(get(
                urlPathEqualTo("/v1/numbers"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-numbers-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated?" + queryString)
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/numbers-list-response.json"), response, false);

        mockNumbersService.verify(getRequestedFor(urlPathEqualTo("/v1/numbers"))
                .withQueryParam("pageSize", equalTo(expectedPageSize))
                .withQueryParam("token", equalTo(expectedToken))
                .withQueryParam("country", equalTo(expectedCountry))
                .withQueryParam("serviceTypes", equalTo(expectedServiceTypes))
                .withQueryParam("exactServiceTypes", equalTo(expectedExactServiceTypes))
                .withQueryParam("matching", equalTo(expectedMatching))
                .withQueryParam("assigned", equalTo("false"))
                .withQueryParam("classification", equalTo(expectedClassification))
                .withQueryParam("availableBy", matching("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}).+Z")));
    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledGetDedicatedNumbers() throws Exception {
        getRest(null, false)
                .when()
                .get("/messaging/numbers/dedicated?country=AU")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldGetDedicatedNumber() throws Exception {
        // Given
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID)
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-response.json"), response, false);

    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledGetDedicatedNumber() throws Exception {
        getRest(null, false)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID)
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldGetDedicatedNumberAssignments() throws Exception {
        // Given
        mockNumbersService.stubFor(
                get(urlEqualTo("/v1/numbers/assignments?vendorId=vendorIdTest0&accountId=accountIdTest0&pageSize=50&token=&country=&serviceTypes="
                        + "&matching=&label=&classification=&status=&accounts=&matchings="))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignments-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated/assignments")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-list-response.json"), response, false);
    }

    @Test
    public void shouldGetDedicatedNumberAssignmentsWithFilters() throws Exception {
        // Given
        mockNumbersService.stubFor(
                get(urlEqualTo("/v1/numbers/assignments?vendorId=vendorIdTest0&accountId=accountIdTest0&pageSize=5"
                        + "&token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891&country=AU&serviceTypes=SMS&matching=abc&label=xyz"
                        + "&classification=GOLD&status=PENDING&accounts=&matchings="))
                        .willReturn(
                                aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignments-response.json"))
                                        .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated/assignments"
                        + "?country=AU&matching=abc&label=xyz&page_size=5&service_types=SMS&classification=GOLD"
                        + "&token=db4cfd5e-e5fe-4e88-a8b0-9c171f95c891&status=PENDING")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-list-response.json"), response, false);
    }

    @Test
    public void shouldGetDedicatedNumberAssignment() throws Exception {
        // Given
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(null, true)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-response.json"), response, false);

    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledGetDedicatedNumberAssignment() throws Exception {
        getRest(null, false)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldCreateDedicatedNumberAssignment() throws Exception {
        // Given
        mockNumbersService.stubFor(post(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(201).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(pathToString("/number-assignment-request.json"), true)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-response.json"), response, false);

    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledCreateDedicatedNumberAssignment() throws Exception {
        getRest(pathToString("/number-assignment-request.json"), false)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldUpdateDedicatedNumberAssignment() throws Exception {
        // Given
        mockNumbersService.stubFor(patch(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(pathToString("/number-assignment-request.json"), true)
                .when()
                .patch("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-response.json"), response, false);

    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledUpdateDedicatedNumberAssignment() throws Exception {
        getRest(pathToString("/number-assignment-request.json"), false)
                .when()
                .patch("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldDeleteDedicatedNumberAssignment() throws Exception {
        // Given
        mockNumbersService.stubFor(delete(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(204).withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        getRest(null, true)
        .when()
        .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
        .then().assertThat()
        .statusCode(204);

    }

    @Test
    public void shouldReturnForbiddenWhenFeatureNotEnabledDeleteDedicatedNumberAssignment() throws Exception {
        getRest(null, false)
                .when()
                .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(FORBIDDEN.value());
    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedUpdateNumberStatus() throws Exception {
        // When
        getRest("{}", false)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/verification")
                .then().assertThat()
                .statusCode(403)
                .contentType(ContentType.JSON);
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
