/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.messagemedia.framework.jackson.core.valuewithnull.ValueWithNull;
import com.messagemedia.numbers.service.client.models.validator.ValidLabel;
import com.messagemedia.restapi.numbers.validation.ValidMapKeys;
import com.messagemedia.restapi.numbers.validation.ValidMapValues;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.Size;
import java.util.Map;

public class UpdateAssignment {

    @JsonProperty("metadata")
    private ValueWithNull<Map<String, String>> metadata;

    @JsonProperty("label")
    private ValueWithNull<String> label;

    public UpdateAssignment() {
    }

    public UpdateAssignment(ValueWithNull<Map<String, String>> metadata, ValueWithNull<String> label) {
        this.metadata = metadata;
        this.label = label;
    }

    public ValueWithNull<Map<String, String>> getWrappedMetadata() {
        return metadata;
    }

    @Size(max = 5)
    @ValidMapKeys(maxLength = 100)
    @ValidMapValues(maxLength = 256)
    public Map<String, String> getMetadata() {
        return null != metadata ? metadata.get() : null;
    }

    public void setMetadata(ValueWithNull<Map<String, String>> metadata) {
        this.metadata = metadata;
    }

    public ValueWithNull<String> getWrappedLabel() {
        return label;
    }

    @ValidLabel
    public String getLabel() {
        return null != label ? label.get() : null;
    }

    public void setLabel(ValueWithNull<String> label) {
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
