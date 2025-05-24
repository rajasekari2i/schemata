package com.opsbeach.sharedlib.utils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * <p>
 * YamlPropertySourceFactory class for loading properties.
 * </p>
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * Create a {@link PropertySource} that wraps the given resource.
     *
     * @param name     the name of the property source
     * @param resource the resource (potentially encoded) to wrap
     * @return the new {@link PropertySource} (never {@code null})
     * @throws IOException if resource resolution failed
     */
    @Override
    public PropertySource<?> createPropertySource(String name, @NonNull EncodedResource resource) throws IOException {
        var properties = load(resource);
        return new PropertiesPropertySource(name != null ? name : Objects.requireNonNull(resource.getResource().getFilename(), "File not found"), Objects.requireNonNullElse(properties, null));
    }

    /**
     * Load properties from the YAML file.
     *
     * @param resource Instance of {@link EncodedResource}
     * @return instance of properties
     */
    private Properties load(EncodedResource resource) throws FileNotFoundException {
        try {
            var factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (IllegalStateException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof FileNotFoundException)
                throw (FileNotFoundException) cause;
            throw ex;
        }
    }
}