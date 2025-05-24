package com.opsbeach.sharedlib.exception;

public class SchemaParserException extends RuntimeException {
    public SchemaParserException(String message) {
      super(message);
    }
  
    public SchemaParserException(String message, Throwable cause) {
      super(message, cause);
    }
}