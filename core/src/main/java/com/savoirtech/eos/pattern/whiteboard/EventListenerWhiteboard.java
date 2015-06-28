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

import com.savoirtech.eos.util.ServiceProperties;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.osgi.framework.BundleContext;

/**
 * A "whiteboard pattern" implementation which maintains a list of event listeners using a
 * {@link EventListenerSupport} object.
 *
 * @param <L> the event interface
 */
public class EventListenerWhiteboard<L> extends AbstractWhiteboard<L, L> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private EventListenerSupport<L> listenerSupport;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new EventListenerWhiteboard which tracks services of the specified listener type and
     * adds them to its {@link EventListenerSupport}.
     *
     * @param bundleContext the bundle context
     * @param listenerType the listener interface
     */
    public EventListenerWhiteboard(BundleContext bundleContext, Class<L> listenerType) {
        super(bundleContext, listenerType);
        this.listenerSupport = new EventListenerSupport<>(listenerType, getClass().getClassLoader());
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected L addService(L service, ServiceProperties props) {
        listenerSupport.addListener(service);
        return service;
    }

    /**
     * Returns a proxy object which can be used to call listener methods on all
     * of the registered event listeners. All calls made to this proxy will be
     * forwarded to all registered listeners.
     *
     * @return a proxy object which can be used to call listener methods on all
     * of the registered event listeners
     */
    public L fire() {
        return listenerSupport.fire();
    }

    @Override
    protected void removeService(L service, L tracked) {
        listenerSupport.removeListener(service);
    }
}
