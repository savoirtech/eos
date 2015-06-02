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

package com.savoirtech.eos.itest.bundle.factory;

import com.savoirtech.eos.itest.bundle.service.Greeter;
import com.savoirtech.eos.pattern.factory.AbstractManagedServiceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;

import java.util.Dictionary;
import java.util.Hashtable;

public class GreeterFactory extends AbstractManagedServiceFactory<Greeter> {
//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public GreeterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Override
    protected Greeter newService(String pid, Dictionary<String, ?> configProperties) throws ConfigurationException {
        final String language = getProperty("language", true, configProperties);
        final String pattern = getProperty("pattern", true, configProperties);
        return new GreeterImpl(language, pattern);
    }

    @Override
    protected Dictionary<String, ?> serviceProperties(Dictionary<String, ?> configProperties) throws ConfigurationException {
        Hashtable<String,Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("language", configProperties.get("language"));
        return serviceProperties;
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    private static class GreeterImpl implements Greeter {
        private final String language;
        private final String pattern;

        public GreeterImpl(String language, String pattern) {
            this.language = language;
            this.pattern = pattern;
        }

        @Override
        public String getLanguage() {
            return language;
        }

        @Override
        public String sayHello(String name) {
            return String.format(pattern, name);
        }
    }
}
