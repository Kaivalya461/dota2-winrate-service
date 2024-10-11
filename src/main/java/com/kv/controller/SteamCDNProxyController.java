package com.kv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This service is taking all incoming requests on "/kv-proxy/**" and doing http request.
 * Acting as a Transparent Proxy service for all endpoints mapped after "/kv-proxy/"
 */
@RestController
public class SteamCDNProxyController {
    @Autowired private RestTemplate restTemplate;
    private static final String STEAM_CDN_HOST_URL = "cdn.cloudflare.steamstatic.com";

    @RequestMapping("/kv-proxy/**")
    public ResponseEntity<?> proxyCall(@RequestBody(required = false) String body,
                                       HttpMethod method,
                                       HttpServletRequest request) throws URISyntaxException {
        //Extract Actual URL
        var actualTargetUrl = request.getRequestURI().substring(9);

        URI uri = new URI(
                "https",
                null,
                STEAM_CDN_HOST_URL,
                443,
                actualTargetUrl,
                request.getQueryString(),
                null
        );

        return restTemplate.exchange(uri, method, new HttpEntity<>(body), byte[].class);
    }
}
