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

import java.util.Dictionary;
import java.util.Hashtable;

import com.savoirtech.eos.util.ServiceProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * A whiteboard class that allows you to "decorate" a discovered service and expose the newly-created decorator
 * as a service.
 *
 * @param <S> the service type
 * @param <D> the decorator type
 */
public abstract class DecoratorWhiteboard<S, D> extends AbstractWhiteboard<S, ServiceRegistration<D>> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Class<D> decoratorType;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public DecoratorWhiteboard(BundleContext bundleContext, Class<S> serviceType, Class<D> decoratorType) {
        super(bundleContext, serviceType);
        this.decoratorType = decoratorType;
    }

//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the decorator object for the specified service.
     *
     * @param service the service
     * @param props   the service properties
     * @return the decorator object (can be null indicating we service invalid)
     */
    protected abstract D createDecorator(S service, ServiceProperties props);

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected ServiceRegistration<D> addService(S service, ServiceProperties props) {
        D decorator = createDecorator(service, props);
        if (decorator == null) {
            return null;
        }
        Dictionary<String, ?> decoratorServiceProps = decoratorServiceProps(props);
        getLogger().info("Registering decorator service {} with properties {}", decoratorType.getSimpleName(), decoratorServiceProps);
        return getBundleContext().registerService(decoratorType, decorator, decoratorServiceProps);
    }

    /**
     * Creates the service properties to be used for the decorator service
     *
     * @param serviceProperties the service properties
     * @return the decorator service properties
     */
    protected Dictionary<String, ?> decoratorServiceProps(ServiceProperties serviceProperties) {
        return new Hashtable<>();
    }

    @Override
    protected void removeService(S service, ServiceRegistration<D> tracked) {
        getLogger().info("Unregistering decorator service {}.", decoratorType.getSimpleName());
        tracked.unregister();
    }
}
