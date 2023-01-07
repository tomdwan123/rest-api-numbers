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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RotaryNumbersService {

    private final ServiceAccountManagementClient serviceAccountManagementClient;

    @Autowired
    public RotaryNumbersService(ServiceAccountManagementClient serviceAccountManagementClient) {
        this.serviceAccountManagementClient = serviceAccountManagementClient;
    }

    public Set<RotaryNumber> getRotaryNumbers(AccountId accountId) throws ServiceAccountManagementException {
        Account account = serviceAccountManagementClient.getAccount(accountId);
        return account.getProviderWeights().stream().filter(this::isRotaryProviderWeight).map(
                providerWeight -> new RotaryNumber(providerWeight.getValue().get())).collect(Collectors.toSet());
    }

    private boolean isRotaryProviderWeight(ProviderWeight providerWeight) {
        return providerWeight.getAddressType().isPresent() && AddressType.INTERNATIONAL.equals(providerWeight.getAddressType().get())
                && ProviderWeightPresence.WEIGHTED.equals(providerWeight.getPresence())
                && "SOURCE_ADDRESS".equals(providerWeight.getType());
    }
}
