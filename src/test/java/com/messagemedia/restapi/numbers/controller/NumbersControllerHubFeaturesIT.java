/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.messagemedia.framework.json.JsonFastMapper;
import com.messagemedia.framework.json.JsonFastMapperImpl;
import com.messagemedia.service.middleware.security.FeatureSet;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.messagemedia.framework.test.IntegrationTestUtilities.getWebContainerPort;
import static com.messagemedia.framework.test.IntegrationTestUtilities.pathToString;
import static com.messagemedia.restapi.common.accounts.AccountFeatureChecker.ACCOUNT_FEATURES_1;
import static com.messagemedia.restapi.numbers.TestData.ACCOUNT_ID;
import static com.messagemedia.restapi.numbers.TestData.NUMBER_ID;
import static com.messagemedia.restapi.numbers.TestData.VENDOR_ID;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_CREATE_ASSIGNMENT;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_DELETE_ASSIGNMENT;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_GET_ASSIGNMENT;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_GET_NUMBER;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_LIST_ASSIGNMENTS;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_LIST_NUMBERS;
import static com.messagemedia.restapi.numbers.controller.NumbersController.HUB_FEATURE_UPDATE_ASSIGNMENT;
import static com.messagemedia.restapi.numbers.controller.NumbersController.SUPPORT_HUB_FEATURE_UPDATE_NUMBER;
import static com.messagemedia.service.accountmanagement.client.model.account.feature.AccountFeatureFlag.SELF_SERVE_DEDICATED_NUMBERS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class NumbersControllerHubFeaturesIT {

    private static final String VENDOR_HEADER = "Vendor-Id";
    private static final String EFFECTIVE_ACCOUNT_HEADER = "Effective-Account-Id";
    private static final String AUTHENTICATED_ACCOUNT_HEADER = "Authenticated-Account-Id";
    private static final String FEATURE_SWITCH = String.valueOf(SELF_SERVE_DEDICATED_NUMBERS.getBitMask());
    private static final String HUB_FEATURE_SET_ID_HEADER = "user-feature-set-id";
    private static final String API_SERVICE_ENDPOINT = "http://localhost:" + getWebContainerPort();
    private static final Integer NUMBERS_SERVICE_PORT = 10153;
    private static final Integer HUB_MIDDLEWARE_PORT = 10154;

    private static final JsonFastMapper MAPPER = new JsonFastMapperImpl(new ObjectMapper());
    @ClassRule
    public static WireMockRule mockNumbersService = new WireMockRule(NUMBERS_SERVICE_PORT);
    @ClassRule
    public static WireMockRule mockHubMiddleware = new WireMockRule(HUB_MIDDLEWARE_PORT);
    private RequestSpecification rest;

    @Before
    public void setup() {
        rest = RestAssured.with().urlEncodingEnabled(false).baseUri(API_SERVICE_ENDPOINT).basePath("/v1").log().all();
        WireMock.reset();
        WireMock.resetAllRequests();
    }

    @Test
    public void shouldGetDedicatedNumbers() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(get(
                urlPathEqualTo("/v1/numbers"))
                .withQueryParam("pageSize", equalTo("50"))
                .withQueryParam("token", equalTo(""))
                .withQueryParam("country", equalTo("AU"))
                .withQueryParam("serviceTypes", equalTo(""))
                .withQueryParam("matching", equalTo(""))
                .withQueryParam("assigned", equalTo("false"))
                .withQueryParam("classification", equalTo(""))
                .withQueryParam("availableBy", matching("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}).+Z"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-numbers-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                )
        );

        // When
        String response = getResponseGet(featureSetId, "/messaging/numbers/dedicated?country=AU");

        // Then
        assertEquals(pathToString("/numbers-list-response.json"), response, false);
    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedGetDedicatedNumbers() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_LIST_NUMBERS, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated?country=AU")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldReturn403WhenNoFeaturesGetDedicatedNumbers() throws Exception {
        // When
        String featureSetId = UUID.randomUUID().toString();
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated?country=AU")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldGetDedicatedNumber() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersServiceGet("/v1/numbers/" + NUMBER_ID, "/numbers-service-number-response.json");

        // When
        String response = getResponseGet(featureSetId, "/messaging/numbers/dedicated/" + NUMBER_ID);

        // Then
        assertEquals(pathToString("/number-response.json"), response, false);
    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedGetDedicatedNumber() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_GET_NUMBER, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID)
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);

    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedUpdateNumber() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(SUPPORT_HUB_FEATURE_UPDATE_NUMBER, featureSetId);

        // When
        getRest(pathToString("/number-update-request.json"), featureSetId)
                .when()
                .patch("/messaging/numbers/dedicated/" + NUMBER_ID)
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldUpdateNumberStatus() throws Exception {
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));
        mockNumbersService.stubFor(patch(urlEqualTo("/v1/numbers/" + NUMBER_ID))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-update-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        String response = getRest("{}", featureSetId)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/verification")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        assertEquals(pathToString("/number-update-response.json"), response, false);
    }

    @Test
    public void shouldReturnForbiddenWhenAccountNotOwnUpdatedNumber() throws Exception {
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(get(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignment-with-different-account-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        getRest("{}", featureSetId)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/verification")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldReturn403WhenNoFeaturesUpdateNumberStatus() throws Exception {
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_LIST_ASSIGNMENTS, featureSetId);

        getRest("{}", featureSetId)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/verification")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldGetDedicatedNumberAssignments() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersServiceGet("/v1/numbers/assignments?vendorId=vendorIdTest0&accountId=accountIdTest0&pageSize=50&token=&country=&serviceTypes=&"
                        + "matching=&label=&classification=&status=&accounts=&matchings=", "/numbers-service-number-assignments-response.json");

        // When
        String response = getRest(null, featureSetId)
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
    public void shouldReturn403WhenFeatureNotAllowedGetDedicatedNumberAssignments() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_LIST_ASSIGNMENTS, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated/assignments")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldGetDedicatedNumberAssignmentsCrossAccounts() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersServiceGet("/v1/numbers/assignments?vendorId=vendorIdTest0&accountId=accountIdTest0&pageSize=50&token=&country=&serviceTypes=&"
                + "matching=&label=&classification=&status=&accounts=all&matchings=", "/numbers-service-number-assignments-response.json");

        // When
        String response = getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated/accounts/assignments")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/number-assignment-list-response.json"), response, false);
    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedGetDedicatedNumberAssignmentsCrossAccount() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(SUPPORT_HUB_FEATURE_UPDATE_NUMBER, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated/accounts/assignments")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldGetDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersServiceGet("/v1/numbers/" + NUMBER_ID + "/assignment", "/numbers-service-number-assignment-response.json");

        // When
        String response = getRest(null, featureSetId)
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
    public void shouldReturn403WhenFeatureNotAllowedGetDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_GET_ASSIGNMENT, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .get("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(403);
    }

    @Test
    public void shouldCreateDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(post(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(201).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(pathToString("/number-assignment-request.json"), featureSetId)
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
    public void shouldReturn403WhenFeatureNotAllowedCreateDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_CREATE_ASSIGNMENT, featureSetId);

        // When
        getRest(pathToString("/number-assignment-request.json"), featureSetId)
                .when()
                .post("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldUpdateDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(patch(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/numbers-service-number-assignment-response.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest(pathToString("/number-assignment-request.json"), featureSetId)
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
    public void shouldReturn403WhenFeatureNotAllowedUpdateDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_UPDATE_ASSIGNMENT, featureSetId);

        // When
        getRest(pathToString("/number-assignment-request.json"), featureSetId)
                .when()
                .patch("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(403).contentType(ContentType.JSON);
    }

    @Test
    public void shouldDeleteDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(null, featureSetId);
        mockNumbersService.stubFor(delete(urlEqualTo("/v1/numbers/" + NUMBER_ID + "/assignment"))
                .willReturn(
                        aResponse().withStatus(204).withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        getRest(null, featureSetId)
                .when()
                .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(204);
    }

    @Test
    public void shouldReturn403WhenFeatureNotAllowedDeleteDedicatedNumberAssignment() throws Exception {
        // Given
        String featureSetId = UUID.randomUUID().toString();
        mockHubMiddleware(HUB_FEATURE_DELETE_ASSIGNMENT, featureSetId);

        // When
        getRest(null, featureSetId)
                .when()
                .delete("/messaging/numbers/dedicated/" + NUMBER_ID + "/assignment")
                .then().assertThat()
                .statusCode(403);
    }

    private String getResponseGet(String featureSetId, String path) {
        return getRest(null, featureSetId)
                .when()
                .get(path)
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();
    }

    private RequestSpecification getRest(String body, String featureSetId) {
        RequestSpecification requestSpecification = rest
                .given()
                .header(VENDOR_HEADER, VENDOR_ID)
                .header(AUTHENTICATED_ACCOUNT_HEADER, ACCOUNT_ID)
                .header(EFFECTIVE_ACCOUNT_HEADER, ACCOUNT_ID)
                .header(ACCOUNT_FEATURES_1, FEATURE_SWITCH)
                .header(HUB_FEATURE_SET_ID_HEADER, featureSetId);
        if (StringUtils.isNotBlank(body)) {
            requestSpecification = requestSpecification.body(body).contentType(ContentType.JSON);
        }
        return requestSpecification;
    }

    private void mockNumbersServiceGet(String url, String responsePath) {
        mockNumbersService.stubFor(get(urlEqualTo(url))
                .willReturn(
                        aResponse().withStatus(200).withBody(pathToString(responsePath)).withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                )
        );
    }

    private void mockHubMiddleware(String exclude, String featureSetId) throws Exception {
        mockHubMiddleware.stubFor(get(urlEqualTo("/middleware/iam/v1/feature-sets/" + featureSetId))
                .willReturn(
                        aResponse().withStatus(200).withBody(hubMiddlewareResponse(exclude, featureSetId))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                )
        );
    }

    private String hubMiddlewareResponse(String exclude, String featureSetId) throws Exception {
        FeatureSet response = new FeatureSet();
        response.setId(UUID.fromString(featureSetId));
        Set<String> flags = Stream.of(HUB_FEATURE_LIST_NUMBERS, HUB_FEATURE_LIST_ASSIGNMENTS, HUB_FEATURE_CREATE_ASSIGNMENT,
                HUB_FEATURE_DELETE_ASSIGNMENT, HUB_FEATURE_UPDATE_ASSIGNMENT, SUPPORT_HUB_FEATURE_UPDATE_NUMBER)
                .collect(Collectors.toSet());
        if (StringUtils.isNotBlank(exclude)) {
            flags.remove(exclude);
        }
        response.setFeatures(flags);
        return MAPPER.toJsonString(response);
    }
}
