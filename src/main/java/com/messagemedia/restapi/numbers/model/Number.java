/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.messagemedia.numbers.service.client.models.Classification;
import com.messagemedia.numbers.service.client.models.NumberType;
import com.messagemedia.numbers.service.client.models.ServiceType;
import com.messagemedia.numbers.service.client.models.Status;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class Number {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("country")
    private String country;

    @JsonProperty("type")
    private NumberType type;

    @JsonProperty("classification")
    private Classification classification;

    @JsonProperty("available_after")
    private OffsetDateTime availableAfter;

    @JsonProperty("capabilities")
    private Set<ServiceType> capabilities;

    @JsonProperty("status")
    @JsonInclude(content = JsonInclude.Include.ALWAYS)
    private Status status;

    public Number() {
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public Number(UUID id, String phoneNumber, String country, NumberType type, Classification classification,
                  OffsetDateTime availableAfter, Set<ServiceType> capabilities, Status status) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.type = type;
        this.classification = classification;
        this.availableAfter = availableAfter;
        this.capabilities = capabilities;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public NumberType getType() {
        return type;
    }

    public void setType(NumberType type) {
        this.type = type;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public OffsetDateTime getAvailableAfter() {
        return availableAfter;
    }

    public void setAvailableAfter(OffsetDateTime availableAfter) {
        this.availableAfter = availableAfter;
    }

    public Set<ServiceType> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<ServiceType> capabilities) {
        this.capabilities = capabilities;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("phoneNumber", phoneNumber)
                .append("country", country)
                .append("type", type)
                .append("classification", classification)
                .append("availableAfter", availableAfter)
                .append("capabilities", capabilities)
                .append("status", status)
                .toString();
    }
}
