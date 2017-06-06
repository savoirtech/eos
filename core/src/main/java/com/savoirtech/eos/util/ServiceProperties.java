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

package com.savoirtech.eos.util;

import org.osgi.framework.Bundle;
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

    /**
     * Returns the bundle corresponding to the service.
     *
     * @return the bundle corresponding to the service
     */
    public Bundle getBundle() {
        return serviceReference.getBundle();
    }

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
