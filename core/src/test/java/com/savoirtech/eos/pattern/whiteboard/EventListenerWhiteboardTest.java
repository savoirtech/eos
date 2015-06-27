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
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class EventListenerWhiteboardTest extends OsgiTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    @Mock
    private MyListener listener;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testConstructor() {
        EventListenerWhiteboard<MyListener> whiteboard = new EventListenerWhiteboard<>(bundleContext, MyListener.class);
        assertEquals(bundleContext,whiteboard.getBundleContext());
        assertEquals(MyListener.class, whiteboard.getServiceType());
        assertEquals(EventListenerWhiteboard.class.getName(), whiteboard.getLogger().getName());
    }

    @Test
    public void testWithNoListeners() {
        EventListenerWhiteboard<MyListener> whiteboard = new EventListenerWhiteboard<>(bundleContext, MyListener.class);
        whiteboard.start();
        whiteboard.fire().doSomething("foo");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testWithListeners() throws Exception {
        EventListenerWhiteboard<MyListener> whiteboard = new EventListenerWhiteboard<MyListener>(bundleContext, MyListener.class);
        whiteboard.start();
        registerService(MyListener.class, listener, serviceProps());
        whiteboard.fire().doSomething("foo");
        verify(listener).doSomething("foo");
    }

    @Test
    public void testAfterUnregistered() throws Exception {
        EventListenerWhiteboard<MyListener> whiteboard = new EventListenerWhiteboard<MyListener>(bundleContext, MyListener.class);
        whiteboard.start();
        ServiceRegistration<MyListener> registration = registerService(MyListener.class, listener, serviceProps());
        registration.unregister();
        whiteboard.fire().doSomething("foo");
        verifyNoMoreInteractions(listener);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    public interface MyListener {
        void doSomething(String msg);
    }

}