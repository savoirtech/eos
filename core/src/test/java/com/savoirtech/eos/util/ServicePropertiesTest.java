/*
 * Copyright (c) 2015-2017 Savoir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.savoirtech.eos.util;

import com.savoirtech.eos.test.OsgiTestCase;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ServicePropertiesTest extends OsgiTestCase {

    @Test
    public void testGetBundle() {
        final ServiceRegistration<HelloServiceImpl> registration = registerService(HelloService.class, new HelloServiceImpl(), serviceProps());


        final ServiceReference<HelloService> reference = bundleContext.getServiceReference(HelloService.class);

        final ServiceProperties props = new ServiceProperties(reference);
        assertEquals(bundleContext.getBundle(), props.getBundle());
        assertEquals(reference.getProperty(Constants.SERVICE_ID), props.getServiceId());
        assertEquals(reference.getProperty(Constants.SERVICE_PID), props.getServicePid());
        assertEquals(0, props.getServiceRanking());
    }

}