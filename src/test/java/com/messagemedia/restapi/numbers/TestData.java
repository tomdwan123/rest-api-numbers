/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.messagemedia.numbers.service.client.models.AssignmentDto;
import com.messagemedia.numbers.service.client.models.Classification;
import com.messagemedia.numbers.service.client.models.NumberAssignmentDto;
import com.messagemedia.numbers.service.client.models.NumberAssignmentListResponse;
import com.messagemedia.numbers.service.client.models.NumberDto;
import com.messagemedia.numbers.service.client.models.NumberForwardDto;
import com.messagemedia.numbers.service.client.models.NumberListResponse;
import com.messagemedia.numbers.service.client.models.NumberType;
import com.messagemedia.numbers.service.client.models.PageMetadata;
import com.messagemedia.numbers.service.client.models.ServiceType;
import com.messagemedia.restapi.numbers.model.Assignment;
import com.messagemedia.restapi.numbers.model.Number;
import com.messagemedia.restapi.numbers.model.NumberAssignment;
import com.messagemedia.restapi.numbers.model.NumberForward;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

public class TestData {

    public static final UUID NUMBER_ID = UUID.fromString("b9ee3fe8-2c20-47b1-96e9-c5d12d7ed985");
    public static final String VENDOR_ID = "vendorIdTest0";
    public static final String ACCOUNT_ID = "accountIdTest0";
    public static final Map<String, String> ASSIGNMENT_METADATA = ImmutableMap.of("key1", "value1");
    public static final String ASSIGNMENT_LABEL = "LabelTest0";

    public static NumberListResponse numberListResponse() {
        return new NumberListResponse(
                ImmutableList.of(numberDto(), anotherNumberDto()),
                new PageMetadata(50, UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
        );
    }

    public static NumberDto numberDto() {
        return NumberDto.NumberDtoBuilder.aNumberDto()
                .withId(NUMBER_ID)
                .withProviderId(UUID.fromString("b0747959-311b-41d4-8eba-989bb99e0325"))
                .withAssignedTo(assignmentDto())
                .withCapabilities(ImmutableSet.of(ServiceType.SMS))
                .withClassification(Classification.GOLD)
                .withCountry("AU")
                .withCreated(OffsetDateTime.parse("2019-06-21T04:04:31.707Z"))
                .withPhoneNumber("+614000000000")
                .withType(NumberType.MOBILE)
                .build();
    }

    public static NumberDto numberDtoWithCallCapability() {
        return NumberDto.NumberDtoBuilder.aNumberDto()
                .withAssignedTo(assignmentDto())
                .withId(UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
                .withProviderId(UUID.fromString("b0747959-311b-41d4-8eba-989bb99e0325"))
                .withCapabilities(ImmutableSet.of(ServiceType.CALL, ServiceType.SMS, ServiceType.MMS))
                .withClassification(Classification.SILVER)
                .withCountry("AU")
                .withCreated(OffsetDateTime.parse("2019-06-21T04:04:31.707Z"))
                .withAvailableAfter(OffsetDateTime.parse("2019-06-21T04:04:31.707Z"))
                .withPhoneNumber("+614111111111")
                .withType(NumberType.MOBILE)
                .build();
    }


    public static NumberDto anotherNumberDto() {
        return NumberDto.NumberDtoBuilder.aNumberDto()
                .withId(UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
                .withProviderId(UUID.fromString("b0747959-311b-41d4-8eba-989bb99e0325"))
                .withCapabilities(ImmutableSet.of(ServiceType.SMS, ServiceType.MMS))
                .withClassification(Classification.SILVER)
                .withCountry("AU")
                .withCreated(OffsetDateTime.parse("2019-06-21T04:04:31.707Z"))
                .withAvailableAfter(OffsetDateTime.parse("2019-06-21T04:04:31.707Z"))
                .withPhoneNumber("+614111111111")
                .withType(NumberType.MOBILE)
                .build();
    }

    public static Number number(NumberDto numberDto) {
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

    public static AssignmentDto assignmentDto() {
        return AssignmentDto.AssignmentDtoBuilder.anAssignmentDto()
                .withId(UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
                .withNumberId(NUMBER_ID)
                .withVendorId(VENDOR_ID)
                .withAccountId(ACCOUNT_ID)
                .withCreated(OffsetDateTime.parse("2019-06-21T04:04:35.443Z"))
                .withMetadata(ASSIGNMENT_METADATA)
                .withLabel(ASSIGNMENT_LABEL)
                .build();
    }

    public static AssignmentDto anotherAssignmentDto() {
        return AssignmentDto.AssignmentDtoBuilder.anAssignmentDto()
                .withId(UUID.fromString("b0747959-311b-41d4-8eba-989bb99e0325"))
                .withNumberId(UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
                .withVendorId("Test-Vendor")
                .withAccountId("Test-Account")
                .withCreated(OffsetDateTime.parse("2019-06-21T04:04:35.443Z"))
                .withMetadata(ASSIGNMENT_METADATA)
                .build();
    }

    public static NumberAssignmentListResponse numberAssignmentListResponse() {
        return new NumberAssignmentListResponse(
                ImmutableList.of(numberAssignmentDto(), anotherNumberAssignmentDto()),
                new PageMetadata(50, UUID.fromString("be3cb602-7c00-4c87-ae4b-b8defc04f179"))
        );
    }

    public static NumberAssignmentDto numberAssignmentDto() {
        return new NumberAssignmentDto(numberDto(), numberDto().getAssignedTo());
    }

    public static NumberAssignmentDto anotherNumberAssignmentDto() {
        NumberDto numberDto = anotherNumberDto();
        AssignmentDto assignmentDto = anotherAssignmentDto();
        assignmentDto.setVendorId(VENDOR_ID);
        assignmentDto.setAccountId(ACCOUNT_ID);
        numberDto.setAssignedTo(assignmentDto);
        numberDto.setAvailableAfter(null);
        return new NumberAssignmentDto(numberDto, numberDto.getAssignedTo());
    }

    public static Assignment assignment(AssignmentDto assignmentDto) {
        return new Assignment(
                assignmentDto.getMetadata(),
                assignmentDto.getId(),
                assignmentDto.getNumberId(),
                assignmentDto.getLabel(),
                assignmentDto.getAccountId(),
                assignmentDto.getVendorId()
        );
    }

    public static NumberAssignment numberAssignment(NumberAssignmentDto numberAssignmentDto) {
        return new NumberAssignment(number(numberAssignmentDto.getNumber()), assignment(numberAssignmentDto.getAssignment()));
    }

    public static NumberForward numberForward(NumberForwardDto numberForwardDto) {
        return new NumberForward(numberForwardDto.getDestination());
    }

    public static NumberForwardDto numberForwardDto() {
        return new NumberForwardDto(randomNumeric(10));
    }
}
