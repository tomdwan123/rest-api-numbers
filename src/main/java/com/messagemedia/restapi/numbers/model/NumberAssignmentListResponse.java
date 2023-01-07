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

import java.util.List;

public class NumberAssignmentListResponse {

    @JsonProperty("data")
    private List<NumberAssignment> data;

    @JsonProperty("pagination")
    private Pagination pagination;

    public NumberAssignmentListResponse() {
    }

    public NumberAssignmentListResponse(List<NumberAssignment> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<NumberAssignment> getData() {
        return data;
    }

    public void setData(List<NumberAssignment> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", data)
                .append("pagination", pagination)
                .toString();
    }
}
