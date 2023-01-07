/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

public class CreateAssignment extends MutableAssignment {

    public CreateAssignment() {
    }

    public CreateAssignment(Map<String, String> metadata, String label) {
        super(metadata, label);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
