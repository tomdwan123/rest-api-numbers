/*
 * Copyright (c) Message4U Pty Ltd 2014-2018
 *
 * Except as otherwise permitted by the Copyright Act 1967 (Cth) (as amended from time to time) and/or any other
 * applicable copyright legislation, the material may not be reproduced in any format and in any way whatsoever
 * without the prior written consent of the copyright owner.
 */

package com.messagemedia.restapi.numbers;

import com.messagemedia.framework.config.BuildVersion;
import com.messagemedia.framework.config.PlatformEnvironmentConfig;
import com.messagemedia.framework.test.HealthPageAsserter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import static com.messagemedia.framework.config.impl.SpringProfileCalculator.DEFAULT_ENVIRONMENT;

@ActiveProfiles(DEFAULT_ENVIRONMENT)
@WebAppConfiguration
@ContextConfiguration(classes = PlatformEnvironmentConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HealthStatusIT {

    private static final int PORT = Integer.parseInt(new BuildVersion().getValue("web.container.port"));

    @Test
    public void testServiceHealth() throws IOException {
        HealthPageAsserter.assertActive("localhost", PORT);
    }
}
