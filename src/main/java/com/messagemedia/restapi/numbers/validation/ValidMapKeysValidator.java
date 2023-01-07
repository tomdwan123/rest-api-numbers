/*
 * Copyright (c) Message4U Pty Ltd 2014-2019
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers.validation;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

public class ValidMapKeysValidator implements ConstraintValidator<ValidMapKeys, Map<String, String>> {

    private int maxLength;

    @Override
    public void initialize(ValidMapKeys annotation) {
        this.maxLength = annotation.maxLength();
    }

    @Override
    public boolean isValid(Map<String, String> map, ConstraintValidatorContext context) {
        if (MapUtils.isEmpty(map)) {
            return true;
        }
        for (String key : map.keySet()) {
            if (StringUtils.length(key) > maxLength) {
                createKeyTooLongErrorMessage(context, key);

                return false;
            }
            if (StringUtils.isBlank(key)) {
                createKeyBlankErrorMessage(context);
                return false;
            }
        }
        return true;
    }

    private void createKeyTooLongErrorMessage(ConstraintValidatorContext context, String key) {
        String message = String.format("Length of key %s exceeds %d", key, maxLength);

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }


    private void createKeyBlankErrorMessage(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("metadata key must be populated").addConstraintViolation();
    }
}
