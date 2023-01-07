/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.controller;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.messagemedia.framework.test.IntegrationTestUtilities.getWebContainerPort;
import static com.messagemedia.framework.test.IntegrationTestUtilities.pathToString;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class RotaryNumbersControllerIT {

    private static final String VENDOR_HEADER = "Vendor-Id";
    private static final String EFFECTIVE_ACCOUNT_HEADER = "Effective-Account-Id";
    private static final String AUTHENTICATED_ACCOUNT_HEADER = "Authenticated-Account-Id";

    private static final String API_SERVICE_ENDPOINT = "http://localhost:" + getWebContainerPort();
    private static final Integer AMS_PORT = 9099;

    @ClassRule
    public static WireMockRule mockAms = new WireMockRule(AMS_PORT);

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
    public void shouldGetRotaryNumbers() throws JSONException {
        // Given
        mockAms.stubFor(get(urlEqualTo("/v1/api/accounts/FunGuys007?effectiveFeatures=false"))
                .withHeader(VENDOR_HEADER, matching("MessageMedia"))
                .withHeader(EFFECTIVE_ACCOUNT_HEADER, matching("FunGuys007")).willReturn(
                        aResponse().withStatus(200).withBody(pathToString("/ams_account.json"))
                                .withHeader("Content-Type", APPLICATION_JSON_UTF8_VALUE)));

        // When
        String response = getRest()
                .when()
                .get("/messaging/numbers/rotary")
                .then().assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body().asString();

        // Then
        assertEquals(pathToString("/rotary_numbers.json"), response, false);
    }

    private RequestSpecification getRest() {
        return rest
                .given()
                .header(VENDOR_HEADER, "MessageMedia")
                .header(AUTHENTICATED_ACCOUNT_HEADER, "FunGuys")
                .header(EFFECTIVE_ACCOUNT_HEADER, "FunGuys007");
    }
}
