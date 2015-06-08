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

import com.google.common.collect.MapMaker;
import com.savoirtech.eos.util.TypeVariableUtils;
import org.apache.commons.lang3.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * Superclass for implementing {@link ManagedServiceFactory} implementations.
 *
 * @param <T> the service type
 */
public abstract class AbstractManagedServiceFactory<T> implements ManagedServiceFactory {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractManagedServiceFactory.class);

    private final Class<T> serviceType;
    private final Map<String, ServiceRegistration<T>> registrations = new MapMaker().concurrencyLevel(5).makeMap();
    private final BundleContext bundleContext;

//----------------------------------------------------------------------------------------------------------------------
// Static Methods
//----------------------------------------------------------------------------------------------------------------------

    protected static String getProperty(String name, Dictionary<String, ?> configProperties) throws ConfigurationException {
        return getProperty(name, true, configProperties);
    }

    protected static String getProperty(String name, boolean required, Dictionary<String, ?> configProperties) throws ConfigurationException {
        final Object value = configProperties.get(name);
        if (value != null) {
            return String.valueOf(value);
        } else if (required) {
            throw new ConfigurationException(name, String.format("Configuration property %s is required.", name));
        } else {
            return null;
        }
    }

    protected static String getProperty(String name, Dictionary<String, ?> configProperties, String defaultValue) {
        final Object value = configProperties.get(name);
        return value == null ? defaultValue : String.valueOf(value);
    }

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public AbstractManagedServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.serviceType = TypeVariableUtils.getTypeVariableBinding(getClass(), AbstractManagedServiceFactory.class, 0);
        Validate.isTrue(serviceType.isInterface(), "Service type %s is not an interface.", serviceType.getName());
    }

    public AbstractManagedServiceFactory(BundleContext bundleContext, Class<T> serviceType) {
        this.serviceType = serviceType;
        this.bundleContext = bundleContext;
        Validate.isTrue(serviceType.isInterface(), "Service type %s is not an interface.", serviceType.getName());
    }

//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new service instance using the configuration properties.
     *
     * @param pid              the service pid
     * @param configProperties the configuration properties
     * @return a new instance of the service type
     * @throws ConfigurationException if the configuration properties are invalid/incomplete
     */
    protected abstract T newService(String pid, Dictionary<String, ?> configProperties) throws ConfigurationException;

//----------------------------------------------------------------------------------------------------------------------
// ManagedServiceFactory Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public void deleted(String pid) {
        final ServiceRegistration<T> registration = registrations.get(pid);
        if (registration != null) {
            LOGGER.info("Unregistering OSGi service for pid \"{}\"...", pid);
            final T service = bundleContext.getService(registration.getReference());
            registration.unregister();
            LOGGER.info("Destroying service object for pid \"{}\"...", pid);
            destroy(pid, service);
        }
    }

    @Override
    public String getName() {
        return String.format("%s service factory", serviceType.getSimpleName());
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
        deleted(pid);
        LOGGER.info("Creating new service object for pid \"{}\"...", pid);
        T service = newService(pid, properties);

        final Dictionary<String, ?> serviceProperties = serviceProperties(properties);
        LOGGER.info("Registering OSGi service for pid \"{}\" using service properties \n{}...", pid, serviceProperties);
        final ServiceRegistration<T> registration = bundleContext.registerService(serviceType, service, serviceProperties);
        registrations.put(pid, registration);
        LOGGER.info("Successfully registered OSGi service for pid \"{}\".", pid);
    }

//----------------------------------------------------------------------------------------------------------------------
// Getter/Setter Methods
//----------------------------------------------------------------------------------------------------------------------

    protected Class<T> getServiceType() {
        return serviceType;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Subclasses can override this method in order to free up any resources consumed by the service instance.  Default
     * implementation is a no-op.
     *
     * @param pid     the service pid
     * @param service the service instance
     */
    protected void destroy(String pid, T service) {
        // Do nothing!
    }

    /**
     * Creates service properties for the given configuration properties.  The default implementation merely returns an
     * empty {@link Hashtable} object.
     *
     * @param configProperties the service configuration properties
     * @return the service properties
     * @throws ConfigurationException if the configuration properties are invalid/incomplete
     */
    protected Dictionary<String, ?> serviceProperties(Dictionary<String, ?> configProperties) throws ConfigurationException {
        return new Hashtable<>();
    }
}