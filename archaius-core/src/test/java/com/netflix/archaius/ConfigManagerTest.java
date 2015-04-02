/**
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.archaius;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.archaius.cascade.ConcatCascadeStrategy;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.config.SimpleDynamicConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.loaders.PropertiesConfigReader;
import com.netflix.archaius.property.DefaultPropertyListener;

public class ConfigManagerTest {
    @Test
    public void testBasicReplacement() throws ConfigException {
        SimpleDynamicConfig dyn = new SimpleDynamicConfig("FAST");
        
        DefaultAppConfig config = DefaultAppConfig.builder()
                .withApplicationConfigName("application")
                .build();
        
        config.addConfig(dyn);
        config.addConfig(MapConfig.builder("test")
                        .put("env",    "prod")
                        .put("region", "us-east")
                        .put("c",      123)
                        .build());
        
        Property<String> prop = config.getProperty("abc").asString("defaultValue");
        
        prop.addListener(new DefaultPropertyListener<String>() {
            @Override
            public void onChange(String next) {
                System.out.println("Configuration changed : " + next);
            }
        });
        
        dyn.setProperty("abc", "${c}");
    }
    
    @Test
    public void testDefaultConfiguration() throws ConfigException {
        DefaultAppConfig config = DefaultAppConfig.builder()
                .withApplicationConfigName("application")
                .build();
        
        DefaultConfigLoader loader = DefaultConfigLoader.builder()
                .withDefaultCascadingStrategy(ConcatCascadeStrategy.from("${env}", "${region}"))
                .withConfigReader(new PropertiesConfigReader())
                .build();
                
        config.addConfig(MapConfig.builder("test")
                    .put("env",    "prod")
                    .put("region", "us-east")
                    .build());

        Assert.assertTrue(config.getBoolean("application.loaded"));
    }
}