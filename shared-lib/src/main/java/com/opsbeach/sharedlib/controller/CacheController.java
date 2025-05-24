package com.opsbeach.sharedlib.controller;

import com.opsbeach.sharedlib.response.SuccessResponse;
//import com.opsbeach.sharedlib.service.CacheService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/cache")
public class CacheController {

    /*private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/{hashKey}/{key}")
    public SuccessResponse<Object> getByKey(@PathVariable("hashKey") String hashKey, @PathVariable("key") String key) {
        return new SuccessResponse<>(cacheService.get(hashKey, key), HttpStatus.OK);
    }

    @GetMapping("/{hashKey}")
    public SuccessResponse<Object> getAll(@PathVariable("hashKey") String hashKey) {
        return new SuccessResponse<>(cacheService.getAll(hashKey), HttpStatus.OK);
    }*/
}