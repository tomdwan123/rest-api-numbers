/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import java.util.UUID;

public class Assignment extends MutableAssignment {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("number_id")
    private UUID numberId;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("vendor_id")
    private String vendorId;

    public Assignment() {
    }

    public Assignment(Map<String, String> metadata, UUID id, UUID numberId, String label, String accountId, String vendorId) {
        super(metadata, label);
        this.id = id;
        this.numberId = numberId;
        this.accountId = accountId;
        this.vendorId = vendorId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNumberId() {
        return numberId;
    }

    public void setNumberId(UUID numberId) {
        this.numberId = numberId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("id", id)
                .append("numberId", numberId)
                .append("accountId", accountId)
                .append("vendorId", vendorId)
                .toString();
    }
}
