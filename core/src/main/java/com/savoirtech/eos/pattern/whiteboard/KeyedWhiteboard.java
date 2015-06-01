package com.savoirtech.eos.pattern.whiteboard;

import com.google.common.collect.MapMaker;
import com.savoirtech.eos.util.ServiceProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * A "whiteboard pattern" implementation which allows you to look up the service objects by a "key."
 *
 * @param <K> the key type
 * @param <T> the service type
 */
public class KeyedWhiteboard<K, T> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyedWhiteboard.class);
    private final ServiceTracker<T, T> serviceTracker;
    private final BundleContext bundleContext;
    private final Map<K, T> serviceMap = new MapMaker().concurrencyLevel(5).makeMap();
    private final Map<Long, K> keysMap = new MapMaker().concurrencyLevel(5).makeMap();
    private final BiFunction<T, ServiceProperties, K> keyFunction;
    private final Class<T> serviceType;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new KeyedWhiteboard which trackes service of the prescribed service type, mapping them to keys using
     * the given key function.
     *
     * @param bundleContext the bundle context
     * @param serviceType the service type
     * @param keyFunction the function that maps the {@link com.savoirtech.eos.util.ServiceProperties} to the key value
     */
    public KeyedWhiteboard(BundleContext bundleContext, Class<T> serviceType, BiFunction<T, ServiceProperties, K> keyFunction) {
        this.bundleContext = bundleContext;
        this.keyFunction = keyFunction;
        this.serviceType = serviceType;
        serviceTracker = new ServiceTracker<>(bundleContext, serviceType, new TrackerCustomizer());
        LOGGER.info("Opening ServiceTracker to search for {} services...", serviceType.getCanonicalName());
        serviceTracker.open(true);
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the current number of services registered.
     * @return the current number of services registered
     */
    public int getServiceCount() {
        return serviceMap.size();
    }

    /**
     * Closes this KeyedWhiteboard, cleaning no longer needed resources.  It is not necessary to call this method if you
     * wish for the service references to remain in effect until the owning bundle is stopped, as it will be
     * cleaned up automatically.
     */
    public void close() {
        LOGGER.info("Closing ServiceTracker searching for {} services...", serviceType.getCanonicalName());
        serviceTracker.close();
    }

    /**
     * Retrieves a service object for the given key value
     * @param key the key value
     * @return the service (or null if it doesn't exist)
     */
    public T getService(K key) {
        return serviceMap.get(key);
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private class TrackerCustomizer implements ServiceTrackerCustomizer<T, T> {
        @Override
        public T addingService(ServiceReference<T> reference) {
            final T service = bundleContext.getService(reference);
            final ServiceProperties props = new ServiceProperties(reference);
            K key = keyFunction.apply(service, props);
            if (key != null) {
                LOGGER.info("Service {} from bundle {} assigned key {}.", props.getServiceId(), reference.getBundle().getSymbolicName(), key);
                keysMap.put(props.getServiceId(), key);
                serviceMap.put(key, service);
                return service;
            } else {
                bundleContext.ungetService(reference);
                LOGGER.info("Rejected service {} from bundle {} (no key)", props.getServiceId(), reference.getBundle().getSymbolicName());
            }
            return null;
        }

        @Override
        public void modifiedService(ServiceReference<T> reference, T service) {
            final ServiceProperties props = new ServiceProperties(reference);
            final Long serviceId = props.getServiceId();
            final K currentKey = keysMap.get(serviceId);
            if (currentKey != null) {
                final K newKey = keyFunction.apply(service, props);
                if (!currentKey.equals(newKey)) {
                    if (newKey != null) {
                        LOGGER.info("Service {} from bundle {} has changed keys ({} -> {}).", props.getServiceId(), reference.getBundle().getSymbolicName(), currentKey, newKey);
                        serviceMap.remove(currentKey);
                        serviceMap.put(newKey, service);
                        keysMap.put(serviceId, newKey);
                    } else {
                        LOGGER.info("Service {} from bundle {} is no longer a valid service.", props.getServiceId(), reference.getBundle().getSymbolicName());
                        serviceTracker.remove(reference);
                    }
                }
            } else {
                LOGGER.warn("Service {} from bundle {} not currently registered.", serviceId, reference.getBundle().getSymbolicName());
                serviceTracker.remove(reference);
            }
        }

        @Override
        public void removedService(ServiceReference<T> reference, T service) {
            final ServiceProperties props = new ServiceProperties(reference);
            final Long serviceId = props.getServiceId();
            final K key = keysMap.get(serviceId);
            if (key != null) {
                LOGGER.info("Service {} from bundle {} with key {} was removed.", serviceId, reference.getBundle().getSymbolicName(), key);
                serviceMap.remove(key);
                keysMap.remove(serviceId);
            }
            bundleContext.ungetService(reference);
        }
    }
}