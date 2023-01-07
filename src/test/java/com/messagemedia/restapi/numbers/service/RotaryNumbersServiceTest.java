/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.service;

import com.messagemedia.domainmodels.accounts.AccountId;
import com.messagemedia.domainmodels.telecommunication.AddressType;
import com.messagemedia.restapi.numbers.model.RotaryNumber;
import com.messagemedia.service.accountmanagement.client.ServiceAccountManagementClient;
import com.messagemedia.service.accountmanagement.client.exception.ServiceAccountManagementException;
import com.messagemedia.service.accountmanagement.client.model.account.Account;
import com.messagemedia.service.accountmanagement.client.model.account.ProviderWeight;
import com.messagemedia.service.accountmanagement.client.model.account.ProviderWeightPresence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RotaryNumbersServiceTest {

    private static final AccountId ACCOUNT_ID = new AccountId("FunGuys007");

    @Mock
    private ServiceAccountManagementClient serviceAccountManagementClient;

    private RotaryNumbersService service;

    @Before
    public void setup() {
        service = new RotaryNumbersService(serviceAccountManagementClient);
    }

    @Test
    public void shouldFetchAccountAndFilterProviderWeights() throws ServiceAccountManagementException {
        // Given
        Account account = new Account();
        account.setProviderWeights(
                newTreeSet(newArrayList(
                        providerWeight(AddressType.INTERNATIONAL, "SOURCE_ADDRESS", ProviderWeightPresence.WEIGHTED, "1"),
                        providerWeight(AddressType.ALPHANUMERIC, "SOURCE_ADDRESS", ProviderWeightPresence.WEIGHTED, "2"),
                        providerWeight(AddressType.INTERNATIONAL, "DIFFERENT_TYPE", ProviderWeightPresence.WEIGHTED, "3"),
                        providerWeight(AddressType.INTERNATIONAL, "SOURCE_ADDRESS", ProviderWeightPresence.FORBIDDEN, "4"),
                        providerWeight(AddressType.INTERNATIONAL, "SOURCE_ADDRESS", ProviderWeightPresence.WEIGHTED, "5"))));
        when(serviceAccountManagementClient.getAccount(ACCOUNT_ID)).thenReturn(account);

        // When
        Set<RotaryNumber> rotaryNumbers = service.getRotaryNumbers(ACCOUNT_ID);

        // Then
        assertThat(rotaryNumbers.stream().map(RotaryNumber::getNumber).collect(Collectors.toSet()), hasItems("1", "5"));
    }

    private ProviderWeight providerWeight(AddressType addressType, String type, ProviderWeightPresence presence, String value) {
        ProviderWeight pw = new ProviderWeight();
        pw.setAddressType(addressType);
        pw.setType(type);
        pw.setPresence(presence);
        pw.setValue(value);
        return pw;
    }
}
