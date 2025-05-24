package com.opsbeach.connect.schemata.enums;

import java.util.Arrays;


public enum SchemaType {
  ENTITY, EVENT, MODEL, UNKNOWN;

  public static SchemaType get(String schemaType) {
    return Arrays.stream(SchemaType.values()).filter(e -> e.name().equalsIgnoreCase(schemaType)).findAny()
        .orElse(SchemaType.UNKNOWN);
  }

}
