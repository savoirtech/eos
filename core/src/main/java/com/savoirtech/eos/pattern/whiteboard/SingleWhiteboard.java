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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.savoirtech.eos.util.ServiceProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.osgi.framework.BundleContext;

public class SingleWhiteboard<S> extends AbstractWhiteboard<S, S> {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<S> reference = new AtomicReference<S>();

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public SingleWhiteboard(BundleContext bundleContext, Class<S> serviceType) {
        super(bundleContext, serviceType);
        start();
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected S addService(S service, ServiceProperties props) {
        if (reference.compareAndSet(null, service)) {
            return service;
        }
        return null;
    }

    public S getService() {
        return reference.get();
    }

    public S getService(S defaultValue) {
        return ObjectUtils.defaultIfNull(getService(), defaultValue);
    }

    public S getService(Supplier<S> defaultSupplier) {
        return ObjectUtils.defaultIfNull(getService(), defaultSupplier.get());
    }

    @Override
    protected void removeService(S service, S tracked) {
        reference.set(null);
    }
}
