/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.model;

import com.google.common.collect.ImmutableMap;
import com.messagemedia.framework.jackson.core.valuewithnull.ValueWithNull;
import org.junit.Test;

import static com.messagemedia.framework.test.AccessorAsserter.assertGettersAndSetters;
import static com.messagemedia.framework.test.AccessorAsserter.registerTestInstanceFor;
import static com.messagemedia.framework.test.CanonicalAsserter.assertToString;
import static com.messagemedia.restapi.numbers.TestData.ASSIGNMENT_LABEL;
import static com.messagemedia.restapi.numbers.TestData.ASSIGNMENT_METADATA;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UpdateAssignmentTest {

    @Test
    public void assertAccessors() throws Exception {
        registerTestInstanceFor(ValueWithNull.class, ValueWithNull.of(ASSIGNMENT_METADATA));
        registerTestInstanceFor(ValueWithNull.class, ValueWithNull.of(ASSIGNMENT_LABEL));
        assertGettersAndSetters(new UpdateAssignment());
    }

    @Test
    public void testToString() {
        assertToString(new UpdateAssignment(ValueWithNull.of(ASSIGNMENT_METADATA), ValueWithNull.of(ASSIGNMENT_LABEL)));
    }

    @Test
    public void shouldSetMetadataProperly() {
        UpdateAssignment test = new UpdateAssignment();
        test.setMetadata(ValueWithNull.of(ImmutableMap.of("key1", "value1")));
        assertThat(test.getMetadata(), equalTo(ImmutableMap.of("key1", "value1")));
    }

    @Test
    public void shouldSetLabelProperly() {
        UpdateAssignment test = new UpdateAssignment();
        test.setLabel(ValueWithNull.of("labelTest"));
        assertThat(test.getLabel(), equalTo("labelTest"));
    }
}
