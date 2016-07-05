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

import java.util.Map;

import com.savoirtech.eos.test.OsgiTestCase;
import com.savoirtech.eos.util.HelloService;
import com.savoirtech.eos.util.HelloServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

public class KeyedWhiteboardTest extends OsgiTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private KeyedWhiteboard<String, HelloService> whiteboard;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    @SuppressWarnings("deprecation")
    public void createWhiteboard() {
        whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> props.getProperty("language"));
        whiteboard.start();
    }

    @Test
    public void testAddingService() throws Exception {
        HelloService svc = new HelloServiceImpl();
        registerService(HelloService.class, svc, serviceProps().with("language", "English"));

        assertEquals(1, whiteboard.getServiceCount());
        assertSame(svc, whiteboard.getService("English"));
    }

    @Test
    public void testAddingServiceWithNullKey() throws Exception {
        registerService(HelloService.class, new HelloServiceImpl(), serviceProps());
        assertEquals(0, whiteboard.getServiceCount());
    }

    @Test
    public void testAsMap() {
        HelloService svc = new HelloServiceImpl();
        registerService(HelloService.class, svc, serviceProps().with("language", "english"));
        registerService(HelloService.class, svc, serviceProps().with("language", "spanish"));
        Map<String, HelloService> map = whiteboard.asMap();
        assertEquals(2, map.size());
        assertSame(svc, map.get("english"));
        assertSame(svc, map.get("spanish"));
    }

    @Test
    public void testModifiedService() throws Exception {
        HelloService svc = new HelloServiceImpl();
        ServiceRegistration<HelloService> reg = registerService(HelloService.class, svc, serviceProps().with("language", "english"));
        reg.setProperties(serviceProps().with("language", "spanish").build());
        assertEquals(1, whiteboard.getServiceCount());
        assertSame(svc, whiteboard.getService("spanish"));
    }

    @Test
    public void testModifiedServiceWhenKeyInvalid() throws Exception {
        HelloService svc = new HelloServiceImpl();
        ServiceRegistration<HelloService> reg = registerService(HelloService.class, svc, serviceProps().with("language", "english"));
        reg.setProperties(serviceProps().build());
        assertEquals(0, whiteboard.getServiceCount());
    }

    @Test
    public void testWithDuplicateKey() {
        HelloService svc1 = (name) -> String.format("Hello, %s!", name);
        HelloService svc2 = (name) -> String.format("Hola, %s!", name);

        registerService(HelloService.class, svc1, serviceProps().with("language", "english"));
        registerService(HelloService.class, svc2, serviceProps().with("language", "english"));

        assertEquals("Hello, Eos!", whiteboard.getService("english").sayHello("Eos"));
    }

    @Test
    public void testStop() {
        whiteboard.stop();
        assertEquals(0, whiteboard.getServiceCount());
    }
}