package com.opsbeach.sharedlib.service;

import com.opsbeach.sharedlib.exception.EncodeException;
import com.opsbeach.sharedlib.security.RSAMechanism;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.exception.BadRequestException;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class App2AppService {

    private static final String CURRENT_DEVICE = "currentDevice";

    private final RestTemplate restTemplate;
    private final ResponseMessage responseMessage;

    public App2AppService(RestTemplateBuilder restTemplateBuilder, ResponseMessage responseMessage, RSAMechanism rsaMechanism) {
        this.restTemplate = restTemplateBuilder.build();
        this.responseMessage = responseMessage;
    }

    private String encode(String value) {
        log.info("encodeValue value: [{}] ", value);
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new EncodeException(ErrorCode.URL_ENCODE, responseMessage.getErrorMessage(ErrorCode.URL_ENCODE, e.getMessage()));
        }
    }

    private String decode(String value) {
        log.info("decodeValue value: [{}] ", value);
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new EncodeException(ErrorCode.URL_ENCODE, responseMessage.getErrorMessage(ErrorCode.URL_ENCODE, e.getMessage()));
        }
    }

    public static Map<String, String> authorizationHeader(String authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Constants.AUTHORIZATION_HEADER, authToken);
        return header;
    }

    public static Map<String, String> clientHeader(String authToken) {
        Map<String, String> header = new HashMap<>();
        header.put(Constants.CLIENT_ID_HEADER, authToken);
        return header;
    }

    public HttpEntity<Object> setHeaders(Map<String, String> token, Object requestBody) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        token.forEach(headers::set);
        if (Objects.isNull(requestBody)) return new HttpEntity<>(headers);
        return new HttpEntity<>(requestBody, headers);
    }

    public HttpEntity<Object> setHeaders(Object requestBody) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (Objects.isNull(requestBody)) return new HttpEntity<>(headers);
        return new HttpEntity<>(requestBody, headers);
    }

    public <T> T httpPatch(String resourceUrl, String body, Map<String, String> headers, Class<T> responseClass) {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(resourceUrl)).method(HttpMethod.PATCH.name(), HttpRequest.BodyPublishers.ofString(body));
        headers.entrySet().forEach(header -> request.header(header.getKey(), header.getValue()));
        String response = "";
        try {
            var res = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            response = res.body().toString();
        } catch (Exception e) {
            throw new UnAuthorizedException(ErrorCode.HTTP_REST_PRE_CONDITION, this.responseMessage.getErrorMessage(ErrorCode.HTTP_REST_PRE_CONDITION, e.getMessage()));
        }
        var object = JsonUtil.parseJsonResponse(response);
        return getResponseAsObject(responseClass, object);
    }

    private <T> T getResponseAsObject(Class<T> responseClass, Object object) {
        return JsonUtil.convertJsonIntoObject(object.toString(), responseClass);
    }

    public <T> T httpGet(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.GET, entity);
        var object = JsonUtil.parseJsonResponse(response);
        return getResponseAsObject(responseClass, object);
    }

    public <T> List<T> httpGetEntities(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.GET, entity);
        return JsonUtil.jsonArrayToObjectList(response, responseClass);
    }

    public <T> T httpPut(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.PUT, entity);
        var object = JsonUtil.parseJsonResponse(response);
        return getResponseAsObject(responseClass, object);
    }

    public <T> T httpDelete(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.DELETE, entity);
        if (Objects.isNull(response)) return null;
        var object = JsonUtil.parseJsonResponse(response);
        return getResponseAsObject(responseClass, object);
    }

    public <T> T httpPost(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.POST, entity);
        var object = JsonUtil.parseJsonResponse(response);
        return getResponseAsObject(responseClass, object);
    }

    public <T> List<T> httpPostEntities(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.POST, entity);
        var object = JsonUtil.parseJsonResponse(response);
        return JsonUtil.jsonArrayToObjectList(object.toString(), responseClass);
    }

    public <T> T httpPostWithoutParsingJson(String resourceUrl, HttpEntity<Object> entity, Class<T> responseClass) {
        var response = getHttpResponse(resourceUrl, HttpMethod.POST, entity);
        return getResponseAsObject(responseClass, response);
    }

    public String getHttpResponse(String resourceUrl, HttpMethod httpMethod, HttpEntity<Object> httpEntity) {
        String httpMethodName = httpMethod.name();
        log.info("Accessing {} with HttpMethod {}", resourceUrl, httpMethodName);
        var response = restTemplateExchange(resourceUrl, httpMethod, httpEntity, String.class);
        if(Objects.isNull(response)) return null;
        return String.valueOf(response);
    }

    public <T> T restTemplateExchange(String resourceUrl, HttpMethod httpMethod, HttpEntity<Object> httpEntity, Class<T> responseClass) {
        try {
            return restTemplate.exchange(resourceUrl, httpMethod, httpEntity, responseClass).getBody();
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, responseMessage.getErrorMessage(ErrorCode.ACCESS_TOKEN_INVALID, exception.getMessage()));
            }
            if (exception.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, exception.getMessage());
            }
            if (exception.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, this.responseMessage.getErrorMessage(ErrorCode.BAD_REQUEST, exception.getMessage()));
            }
            if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new RecordNotFoundException(ErrorCode.DOMIN_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.DOMIN_NOT_FOUND, exception.getMessage()));
            }
            throw new UnAuthorizedException(ErrorCode.HTTP_REST_PRE_CONDITION, this.responseMessage.getErrorMessage(ErrorCode.HTTP_REST_PRE_CONDITION, exception.getMessage()));
        }
    }
}