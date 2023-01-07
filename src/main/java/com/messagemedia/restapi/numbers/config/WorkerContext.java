/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */
package com.messagemedia.restapi.numbers.config;

import com.messagemedia.framework.service.config.RestUtilConfig;
import com.messagemedia.numbers.service.client.config.NumbersServiceClientConfig;
import com.messagemedia.restapi.common.accounts.AccountFeatureChecker;
import com.messagemedia.restapi.common.web.config.RestApiWorkerContext;
import com.messagemedia.service.accountmanagement.client.config.ServiceAccountManagementClientConfig;
import com.messagemedia.service.accountmanagement.client.model.Credentials;
import com.messagemedia.service.middleware.security.client.FeatureSetClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
@ComponentScan("com.messagemedia.restapi.numbers")
@PropertySource("classpath:config.properties")
@Import({
        RestUtilConfig.class,
        ServiceAccountManagementClientConfig.class,
        NumbersServiceClientConfig.class,
        FeatureSetClientConfig.class
})
public class WorkerContext extends RestApiWorkerContext {

    @Bean
    public Credentials amsCredentials(@Nonnull @Value("${service.accountmanagement.api.login.username}") final String username,
                                      @Nonnull @Value("${service.accountmanagement.api.login.password}") final String password) {
        return new Credentials(checkNotNull(username), checkNotNull(password));
    }

    @Bean
    public AccountFeatureChecker accountFeatureChecker() {
        return new AccountFeatureChecker();
    }
}
