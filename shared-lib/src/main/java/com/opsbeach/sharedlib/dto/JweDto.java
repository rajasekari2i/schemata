package com.opsbeach.sharedlib.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JweDto {
    private long userId;
    private String client;
    private String username;
    private Boolean isRefresh;

    public static JSONObject asJsonObject(JweDto jweDto) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var jweJsonString = mapper.writeValueAsString(jweDto);
        var jweJsonObject = new JSONObject();
        jweJsonObject.put("jweDto", jweJsonString);
        return jweJsonObject;
    }

    public static JweDto fromJsonObject(Map<String, Object> jsonObject) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var jweJsonString = String.valueOf(jsonObject.get("jweDto"));
        return mapper.readValue(jweJsonString, JweDto.class);
    }
}
