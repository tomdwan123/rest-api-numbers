/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.validation;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class MapKeysValidatorTest {

    public static class Model {
        private Map<String, String> props;

        @ValidMapKeys(maxLength = 3)
        public Map<String, String> getProps() {
            return props;
        }

        public void setProps(Map<String, String> props) {
            this.props = props;
        }
    }

    private Model object;
    private Validator validator;

    @Before
    public void setUp() {
        object = new Model();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void nullOrEmptyMapIsValid() {
        assertEquals(0, validator.validate(object).size());

        object.setProps(Collections.<String, String>emptyMap());
        assertEquals(0, validator.validate(object).size());
    }

    @Test
    public void keyLengthIsValid() {
        object.setProps(Collections.singletonMap("key", "value"));
        assertEquals(0, validator.validate(object).size());
    }

    @Test
    public void keyLengthIsInvalid() {
        object.setProps(Collections.singletonMap("longKey", "value"));

        Set<ConstraintViolation<Model>> constraintViolations = validator.validate(object);

        assertEquals(1, constraintViolations.size());
        assertEquals("Length of key longKey exceeds 3", constraintViolations.iterator().next().getMessage());
    }

    @Test
    @UseDataProvider("createBlankKey")
    public void blankMetadataKey(String blankKey) throws Exception {
        //GIVEN
        object.setProps(Collections.singletonMap(blankKey, "value"));

        //WHEN
        Set<ConstraintViolation<Model>> constraintViolations = validator.validate(object);

        //THEN
        assertEquals(1, constraintViolations.size());
        assertEquals("metadata key must be populated", constraintViolations.iterator().next().getMessage());
    }

    @DataProvider
    public static Object[][] createBlankKey() {
        return new Object[][]{{""}, {"   "}};
    }
}
