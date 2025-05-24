package com.opsbeach.sharedlib.response;

import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.utils.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("response-messages")
@PropertySource(value = "classpath:application-messages.yaml", factory = YamlPropertySourceFactory.class)
public class ResponseMessage {

    Map<String, String> success = new HashMap<>();

    Map<String, String> error = new HashMap<>();

    public String getErrorMessage(ErrorCode code) {
        return getError().get(Integer.toString(code.getKey()));
    }

    public String getErrorMessage(ErrorCode code, String arg) {
        return MessageFormat.format(getErrorMessage(code), arg);
    }

    public String getErrorMessage(ErrorCode code, List<String> args) {
        return MessageFormat.format(getErrorMessage(code), args);
    }

    public String getErrorMessage(ErrorCode code, String... args) {
        return MessageFormat.format(getErrorMessage(code), args);
    }

    public String getErrorMessage(ErrorCode code, Long args) {
        return MessageFormat.format(getErrorMessage(code), args);
    }

    public String getErrorMessage(ErrorCode code, Integer args) {
        return MessageFormat.format(getErrorMessage(code), args);
    }

    public String getSuccessMessage(SuccessCode code) {
        return getSuccess().get(Integer.toString(code.getKey()));
    }

    public String getSuccessMessage(SuccessCode code, String args) {
        return MessageFormat.format(getSuccessMessage(code), args);
    }
}