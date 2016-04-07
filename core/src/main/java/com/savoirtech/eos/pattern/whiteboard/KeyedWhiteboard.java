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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.common.collect.MapMaker;
import com.savoirtech.eos.util.ServiceProperties;
import org.osgi.framework.BundleContext;

/**
 * A "whiteboard pattern" implementation which allows you to look up the service objects by a "key."
 *
 * @param <K> the key type
 * @param <S> the service type
 */
public class KeyedWhiteboard<K, S> extends AbstractWhiteboard<S, K> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final Map<K, S> serviceMap = new MapMaker().concurrencyLevel(5).makeMap();
    private final BiFunction<S, ServiceProperties, K> keyFunction;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new KeyedWhiteboard which tracks service of the prescribed service type, mapping them to keys using
     * the given key function.
     *
     * @param bundleContext the bundle context
     * @param serviceType   the service type
     * @param keyFunction   the function that maps the {@link com.savoirtech.eos.util.ServiceProperties} to the key value
     */
    public KeyedWhiteboard(BundleContext bundleContext, Class<S> serviceType, BiFunction<S, ServiceProperties, K> keyFunction) {
        super(bundleContext, serviceType);
        this.keyFunction = keyFunction;
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected K addService(S service, ServiceProperties props) {
        K key = keyFunction.apply(service, props);
        if (key != null) {
            serviceMap.put(key, service);
        }
        return key;
    }

    /**
     * Returns the services currently tracked by this whiteboard as a {@link Map} object.
     * @return the map
     */
    public Map<K,S> asMap() {
        return new HashMap<>(serviceMap);
    }

    /**
     * Retrieves a service object for the given key value
     *
     * @param key the key value
     * @return the service (or null if it doesn't exist)
     */
    public S getService(K key) {
        return serviceMap.get(key);
    }

    @Override
    protected void removeService(S service, K tracked) {
        serviceMap.remove(tracked);
    }
}