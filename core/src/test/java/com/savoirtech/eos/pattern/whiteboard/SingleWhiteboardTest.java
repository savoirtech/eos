/*
 * Copyright (c) 2015-2016 Savoir Technologies, Inc.
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SingleWhiteboardTest extends OsgiTestCase {

    private SingleWhiteboard<HelloService> whiteboard;

    @Before
    public void initWhiteboard() {
        whiteboard = new SingleWhiteboard<>(bundleContext, HelloService.class);
    }

    @After
    public void stopWhiteboard() {
        whiteboard.stop();
    }

    @Test
    public void testWithNoServices() {
        assertNull(whiteboard.getService());
    }

    @Test
    public void testDefaultIfNull() {
        HelloService defaultValue = new HelloServiceImpl();

        assertSame(defaultValue, whiteboard.getService(defaultValue));
        assertSame(defaultValue, whiteboard.getService(() -> defaultValue));
    }

    @Test
    public void testWithSingleValue() {
        HelloServiceImpl expected = new HelloServiceImpl();
        registerService(HelloService.class, expected, serviceProps());

        assertEquals(expected, whiteboard.getService());
    }

    @Test
    public void testWithMultipleValues() {
        HelloServiceImpl expected = new HelloServiceImpl();
        registerService(HelloService.class, expected, serviceProps());
        registerService(HelloService.class, new HelloServiceImpl(), serviceProps());
        registerService(HelloService.class, new HelloServiceImpl(), serviceProps());
        assertEquals(expected, whiteboard.getService());
    }
}