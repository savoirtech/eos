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

import com.google.common.collect.MapMaker;
import com.savoirtech.eos.util.ServiceProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract "whiteboard pattern" implementation which uses "tracking objects" to maintain the state of which services
 * are accepted by the whiteboard.
 *
 * @param <S> the service type
 * @param <T> the tracking object type
 */
public abstract class AbstractWhiteboard<S, T> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ServiceTracker<S, S> serviceTracker;
    private final Map<Long, T> trackingObjects = new MapMaker().concurrencyLevel(5).makeMap();
    private final BundleContext bundleContext;
    private final Class<S> serviceType;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new AbstractWhiteboard which tracks service of the prescribed service type, mapping them to keys using
     * the given key function.
     *
     * @param bundleContext the bundle context
     * @param serviceType   the service type
     */
    protected AbstractWhiteboard(BundleContext bundleContext, Class<S> serviceType) {
        this.bundleContext = bundleContext;
        this.serviceType = serviceType;
        this.serviceTracker = new ServiceTracker<>(bundleContext, serviceType, new TrackerCustomizer());
        logger.info("Opening ServiceTracker to search for {} services...", serviceType.getCanonicalName());
        serviceTracker.open(true);
    }

//----------------------------------------------------------------------------------------------------------------------
// Abstract Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Called when a service is added to this whiteboard.  The object returned from this method will be passed
     * to the {@link AbstractWhiteboard#removeService(Object, Object)} method when this same service is removed.  If
     * this service is not accepted by this whiteboard, the return null.
     * @param service the service object
     * @param props the service properties
     * @return the object to be "tracked" for this service
     */
    protected abstract T addService(S service, ServiceProperties props);

    /**
     * Called when a service is removed from this whiteboard.  The "tracked" object is the same object which was
     * returned by the {@link AbstractWhiteboard#addService(Object, ServiceProperties)} method when the the service
     * was added to this whiteboard.
     * @param service the service object
     * @param tracked the tracked object
     */
    protected abstract void removeService(S service, T tracked);

//----------------------------------------------------------------------------------------------------------------------
// Getter/Setter Methods
//----------------------------------------------------------------------------------------------------------------------

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    protected Logger getLogger() {
        return logger;
    }

    public Class<S> getServiceType() {
        return serviceType;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the current number of services registered.
     *
     * @return the current number of services registered
     */
    public int getServiceCount() {
        return trackingObjects.size();
    }

    /**
     * Starts the whiteboard.  This will initiate the search for all matching services.
     *
     * Note: as of version 1.0.9, this method is deprecated, as the service tracker is opened in the constructor.
     */
    @Deprecated
    public void start() {
        logger.warn("The start() method has been deprecated, please discontinue its use.");
    }

    /**
     * Closes this whiteboard, cleaning no longer needed resources.  It is not necessary to call this method if you
     * wish for the service references to remain in effect until the owning bundle is stopped, as it will be
     * cleaned up automatically.
     */
    public void stop() {
        logger.info("Closing ServiceTracker searching for {} services...", serviceType.getCanonicalName());
        serviceTracker.close();
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class TrackerCustomizer implements ServiceTrackerCustomizer<S, S> {
        @Override
        public void modifiedService(ServiceReference<S> reference, S service) {
            removedService(reference, service);
            final ServiceProperties props = new ServiceProperties(reference);
            final T tracked = addService(service, props);
            if (tracked == null) {
                logger.warn("Rejected modified {} service {} from bundle {}.", serviceType.getSimpleName(), props.getServiceId(), reference.getBundle().getSymbolicName());
                serviceTracker.remove(reference);
            } else {
                logger.info("Accepted modified {} service {} (tracked by \"{}\") from bundle {}.", serviceType.getSimpleName(), props.getServiceId(), tracked, reference.getBundle().getSymbolicName());
                trackingObjects.put(props.getServiceId(), tracked);
            }
        }

        @Override
        public void removedService(ServiceReference<S> reference, S service) {
            final ServiceProperties props = new ServiceProperties(reference);
            final T previouslyTracked = trackingObjects.remove(props.getServiceId());
            if (previouslyTracked != null) {
                removeService(service, previouslyTracked);
                bundleContext.ungetService(reference);
            }
        }

        @Override
        public S addingService(ServiceReference<S> reference) {
            final S service = bundleContext.getService(reference);
            final ServiceProperties props = new ServiceProperties(reference);
            final T tracked = addService(service, props);
            if (tracked == null) {
                logger.warn("Rejected {} service {} from bundle {}.",serviceType.getSimpleName(), props.getServiceId(), reference.getBundle().getSymbolicName());
                bundleContext.ungetService(reference);
                return null;
            } else {
                logger.info("Accepted {} service {} (tracked by \"{}\") from bundle {}.", serviceType.getSimpleName(), props.getServiceId(), tracked, reference.getBundle().getSymbolicName());
                trackingObjects.put(props.getServiceId(), tracked);
                return service;
            }
        }
    }
}