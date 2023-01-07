/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RotaryNumber {

    @JsonProperty("phone_number")
    private final String number;

    public RotaryNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "RotaryNumber{" + "number='" + number + '\'' + '}';
    }
}
