/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.messagemedia.framework.test.AccessorAsserter;
import com.messagemedia.framework.test.CanonicalAsserter;
import org.junit.Test;

public class RotaryNumberTest {

    @Test
    public void assertGetters() throws Exception {
        AccessorAsserter.assertGetters(new RotaryNumber("1"));
    }

    @Test
    public void testToString() {
        CanonicalAsserter.assertToString(new RotaryNumber("2"));
    }
}
