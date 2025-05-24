package com.opsbeach.sharedlib.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Json convertor util.
 * </p>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    private static final String JSON_CONVERSION_ERROR = "There is an error in converting the Json to Object - {}";
    private static ResponseMessage responseMessage;

    @Autowired
    public JsonUtil(ResponseMessage responseMessage) {
        JsonUtil.responseMessage = responseMessage;
    }

    public static Object parseJsonResponse(String response) {
        Map<String, Object> jsonObject;
        try {
            jsonObject = JSONObjectUtils.parse(response);
        } catch (ParseException e) {
            log.error("Error occurred during processing the response - {}", e.getMessage());
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
        if(!ObjectUtils.isEmpty(jsonObject.get(Constants.STATUS)) && !ObjectUtils.isEmpty(jsonObject.get(Constants.ENTITY))) {
            var status = jsonObject.get(Constants.STATUS).toString();
            if (!status.equals(Constants.SUCCESS)) {
                throw new InvalidDataException(ErrorCode.DATA_RESPONSE_ERROR, responseMessage.getErrorMessage(ErrorCode.DATA_RESPONSE_ERROR, String.valueOf(jsonObject.get(Constants.MESSAGE))));
            }
            return jsonObject.get(Constants.ENTITY);
        }
        return jsonObject;
    }

    public static <T> T convertJsonIntoObject(String object, Class<T> clazz) {
        JsonMapper jsonMapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
        var javaType = jsonMapper.getTypeFactory().constructType(clazz);
        T result;
        try {
            result = jsonMapper.readValue(object, javaType);
        } catch (JsonProcessingException e) {
            log.error(JSON_CONVERSION_ERROR, e.getMessage());
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
        return result;
    }

    public static <T> List<T> jsonArrayToObjectList(String json, Class<T> tClass) {
        var mapper = new ObjectMapper();
        var listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
        List<T> result;
        try {
            result = mapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            log.error(JSON_CONVERSION_ERROR, e.getMessage());
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
        return result;
    }

    public static Map<String, String> convertJsonToMap(String json) {
        Map<String, String> convertedJson;
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
        };
        var mapper = new ObjectMapper();
        try {
            convertedJson = mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error(JSON_CONVERSION_ERROR, e.getMessage());
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
        return convertedJson;
    }

    /**
     * Converts Object into Json.
     *
     * @param object - Object which needs to be converted to Json.
     * @return String - Converted Json.
     */
    public static String convertObjectIntoJson(Object object) {
        var mapper = new ObjectMapper();
        String convertedJson;
        try {
            convertedJson = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(JSON_CONVERSION_ERROR, e.getMessage());
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
        return convertedJson;
    }
}
