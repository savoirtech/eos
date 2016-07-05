/*
 * Copyright (c) 2015-2015 Savoir Technologies, Inc.
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

package com.savoirtech.eos.pattern.whiteboard;

import com.savoirtech.eos.test.OsgiTestCase;
import com.savoirtech.eos.util.HelloService;
import com.savoirtech.eos.util.HelloServiceImpl;
import com.savoirtech.eos.util.Reverser;
import com.savoirtech.eos.util.ServiceProperties;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class DecoratorWhiteboardTest extends OsgiTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testServiceLifecycle() throws Exception {
        new HelloReverser(bundleContext);
        ServiceReference<?>[] refs = registry.getServiceReferences(Reverser.class.getName(), null);
        assertNull(refs);
        ServiceRegistration<HelloServiceImpl> registration = registerService(HelloService.class, new HelloServiceImpl(), serviceProps());
        refs = registry.getServiceReferences(Reverser.class.getName(), null);
        assertEquals(1, refs.length);
        registration.unregister();
        refs = registry.getServiceReferences(Reverser.class.getName(), null);
        assertNull(refs);

    }


    @Test
    public void testServiceLifecycleWhenNoDecoratorReturned() throws Exception {
        new NullDecorator(bundleContext);
        registerService(HelloService.class, new HelloServiceImpl(), serviceProps());
        ServiceReference<?>[] refs = registry.getServiceReferences(Reverser.class.getName(), null);
        assertNull(refs);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    public static class NullDecorator extends DecoratorWhiteboard<HelloService, Reverser> {
        public NullDecorator(BundleContext bundleContext) {
            super(bundleContext, HelloService.class, Reverser.class);
        }

        @Override
        protected Reverser createDecorator(HelloService service, ServiceProperties props) {
            return null;
        }
    }

    public static class HelloReverser extends DecoratorWhiteboard<HelloService, Reverser> {
        public HelloReverser(BundleContext bundleContext) {
            super(bundleContext, HelloService.class, Reverser.class);
        }

        @Override
        protected Reverser createDecorator(HelloService service, ServiceProperties props) {
            return message -> StringUtils.reverse(service.sayHello(message));
        }
    }
}