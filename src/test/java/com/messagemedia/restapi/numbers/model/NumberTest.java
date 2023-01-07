/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.model;

import org.junit.Test;

import java.time.OffsetDateTime;

import static com.messagemedia.framework.test.AccessorAsserter.assertGettersAndSetters;
import static com.messagemedia.framework.test.AccessorAsserter.registerTestInstanceFor;
import static com.messagemedia.framework.test.CanonicalAsserter.assertToString;
import static com.messagemedia.restapi.numbers.TestData.number;
import static com.messagemedia.restapi.numbers.TestData.numberDto;

public class NumberTest {

    @Test
    public void assertAccessors() throws Exception {
        registerTestInstanceFor(OffsetDateTime.class, OffsetDateTime.now());
        assertGettersAndSetters(new Number());
    }

    @Test
    public void testToString() {
        assertToString(number(numberDto()));
    }
}
