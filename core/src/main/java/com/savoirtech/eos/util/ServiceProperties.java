package com.savoirtech.eos.util;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class ServiceProperties {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final ServiceReference<?> serviceReference;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public ServiceProperties(ServiceReference<?> serviceReference) {
        this.serviceReference = serviceReference;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public <T> T getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Returns the service property value or the default value if the property is not present.
     *
     * @param key          the service property key
     * @param defaultValue the default value
     * @param <T>          the property type
     * @return the property value or the default value
     * @throws ClassCastException if the service property is not of the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        T value = (T) serviceReference.getProperty(key);
        return value == null ? defaultValue : value;
    }

    public Long getServiceId() {
        return getProperty(Constants.SERVICE_ID);
    }

    public String getServicePid() {
        return getProperty(Constants.SERVICE_PID);
    }

    public int getServiceRanking() {
        return getProperty(Constants.SERVICE_RANKING, 0);
    }
}
