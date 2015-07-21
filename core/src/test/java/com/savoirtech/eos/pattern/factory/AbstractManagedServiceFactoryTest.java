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

package com.savoirtech.eos.pattern.factory;

import com.savoirtech.eos.test.MockObjectTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;


public class AbstractManagedServiceFactoryTest extends MockObjectTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    @Mock
    private BundleContext bundleContext;

    @Mock
    private org.osgi.framework.ServiceRegistration<HelloService> serviceRegistration;

    @Mock
    private ServiceReference<HelloService> serviceReference;

    private HelloManagedServiceFactory msf;

    private AtomicInteger destroyCount = new AtomicInteger();

    private Hashtable<String, Object> serviceProperties = new Hashtable<>();

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    @SuppressWarnings("unchecked")
    public void initServiceFactory() {
        msf = new HelloManagedServiceFactory(bundleContext);

        when(bundleContext.registerService(eq(HelloService.class), isA(HelloService.class), isA(Dictionary.class))).thenReturn(serviceRegistration);
        when(serviceRegistration.getReference()).thenReturn(serviceReference);
    }

    @Test
    public void testExplicitConstructor() {
        msf = new HelloManagedServiceFactory(bundleContext, HelloService.class);
        assertEquals(HelloService.class, msf.getServiceType());
    }

    @Test
    public void testConstructor() {
        assertEquals(HelloService.class, msf.getServiceType());
    }

    @Test
    public void testGetName() {
        assertEquals("HelloService service factory", msf.getName());
    }

    @Test
    public void testInitialRegistration() throws Exception {
        final Dictionary<String, Object> configProperties = new Hashtable<>();
        msf.updated("pid", configProperties);
        ArgumentCaptor<HelloService> captor = ArgumentCaptor.forClass(HelloService.class);
        verify(bundleContext).registerService(eq(HelloService.class), captor.capture(), same(serviceProperties));
        verifyNoMoreInteractions(bundleContext, serviceRegistration);
        final HelloService service = captor.getValue();
        assertEquals("Hello, Slappy!", service.sayHello("Slappy"));
    }

    @Test
    public void testSubsequentRegistration() throws Exception {
        final Dictionary<String, Object> configProperties = new Hashtable<>();
        msf.updated("pid", configProperties);

        configProperties.put("greetingPattern", "Hola, %s!");

        msf.updated("pid", configProperties);

        ArgumentCaptor<HelloService> captor = ArgumentCaptor.forClass(HelloService.class);
        verify(bundleContext).getService(serviceReference);
        verify(serviceRegistration).unregister();
        verify(bundleContext, times(2)).registerService(eq(HelloService.class), captor.capture(), same(serviceProperties));
        final HelloService service = captor.getValue();
        assertEquals("Hola, Slappy!", service.sayHello("Slappy"));
        assertEquals(1, destroyCount.get());
    }

    @Test
    public void testGetRequiredProperty() throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("foo", "bar");
        assertEquals("bar", AbstractManagedServiceFactory.getProperty("foo", properties));
    }


    @Test(expected = ConfigurationException.class)
    public void testGetRequiredMissingProperty() throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        assertEquals("bar", AbstractManagedServiceFactory.getProperty("foo", properties));
    }

    @Test
    public void testGetOptionalProperty() throws Exception {
        Dictionary<String, Object> properties = new Hashtable<>();
        assertNull(AbstractManagedServiceFactory.getProperty("foo", false, properties));
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    public interface HelloService {
        String sayHello(String name);
    }

    private final class HelloServiceImpl implements HelloService {
        private final String greetingPattern;

        public HelloServiceImpl(String greetingPattern) {
            this.greetingPattern = greetingPattern;
        }

        @Override
        public String sayHello(String name) {
            return String.format(greetingPattern, name);
        }
    }

    public class HelloManagedServiceFactory extends AbstractManagedServiceFactory<HelloService> {
        public HelloManagedServiceFactory(BundleContext bundleContext) {
            super(bundleContext);
        }

        public HelloManagedServiceFactory(BundleContext bundleContext, Class<HelloService> serviceType) {
            super(bundleContext, serviceType);
        }

        @Override
        protected HelloService newService(String pid, Dictionary<String, ?> configProperties) throws ConfigurationException {
            return new HelloServiceImpl(getProperty("greetingPattern", configProperties, "Hello, %s!"));
        }

        @Override
        protected void destroy(String pid, HelloService service) {
            super.destroy(pid, service);
            destroyCount.incrementAndGet();
        }

        @Override
        protected Dictionary<String, ?> serviceProperties(Dictionary<String, ?> configProperties) throws ConfigurationException {
            final Dictionary<String, ?> superProperties = super.serviceProperties(configProperties);
            assertEquals(0, superProperties.size());
            return serviceProperties;
        }
    }
}