package com.opsbeach.connect.schemata.processor.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Abstracts loading of Descriptor objects from various sources
 */
public interface Loader {
    public List<Descriptor> loadDescriptors() throws Descriptors.DescriptorValidationException;
    public Map<String, FileDescriptor> loadFileDescriptors() throws Descriptors.DescriptorValidationException;
}
