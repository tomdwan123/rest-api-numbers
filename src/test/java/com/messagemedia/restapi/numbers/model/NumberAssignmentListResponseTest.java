/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.model;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.messagemedia.framework.test.AccessorAsserter.assertGettersAndSetters;
import static com.messagemedia.framework.test.CanonicalAsserter.assertToString;
import static com.messagemedia.restapi.numbers.TestData.numberAssignment;
import static com.messagemedia.restapi.numbers.TestData.numberAssignmentDto;

public class NumberAssignmentListResponseTest {

    @Test
    public void assertAccessors() throws Exception {
        assertGettersAndSetters(new NumberAssignmentListResponse());
    }

    @Test
    public void testToString() {
        assertToString(new NumberAssignmentListResponse(ImmutableList.of(numberAssignment(numberAssignmentDto())),
                new Pagination(50, null)));
    }
}
