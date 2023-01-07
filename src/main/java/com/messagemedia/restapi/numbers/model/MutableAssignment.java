/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.messagemedia.numbers.service.client.models.validator.ValidLabel;
import com.messagemedia.restapi.numbers.validation.ValidMapKeys;
import com.messagemedia.restapi.numbers.validation.ValidMapValues;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.Size;
import java.util.Map;

public abstract class MutableAssignment {

    @JsonProperty("metadata")
    @Size(min = 0, max = 5)
    @ValidMapKeys(maxLength = 100)
    @ValidMapValues(maxLength = 256)
    private Map<String, String> metadata;


    @ValidLabel
    @JsonProperty("label")
    private String label;

    protected MutableAssignment() {
    }

    protected MutableAssignment(Map<String, String> metadata, String label) {
        this.metadata = metadata;
        this.label = label;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("metadata", metadata)
                .append("label", label)
                .toString();
    }
}
