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

import java.util.UUID;

public class Pagination {

    @JsonProperty("page_size")
    private int pageSize;

    @JsonProperty("next_token")
    private UUID nextToken;

    public Pagination() {
    }

    public Pagination(int pageSize, UUID nextToken) {
        this.pageSize = pageSize;
        this.nextToken = nextToken;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public UUID getNextToken() {
        return nextToken;
    }

    public void setNextToken(UUID nextToken) {
        this.nextToken = nextToken;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pageSize", pageSize)
                .append("nextToken", nextToken)
                .toString();
    }
}
