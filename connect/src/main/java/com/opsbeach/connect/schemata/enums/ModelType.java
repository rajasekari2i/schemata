package com.opsbeach.connect.schemata.enums;

import java.util.Arrays;


public enum ModelType {
  DIMENSION, FACT, NONE;

  public static ModelType get(String modelType) {
    return Arrays.stream(ModelType.values())
        .filter(e -> e.name().equalsIgnoreCase(modelType)).findAny().orElse(ModelType.NONE);
  }
}
