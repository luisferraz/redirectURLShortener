package com.java.curso.rockeseat.redirectUrlShortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = (String) pathParameters.replace("/", "");

        if (shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("luisprto-curso-java-rocketseat")
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3Object;
        try {
            s3Object = s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error getting object from S3: " + e.getMessage(), e);
        }

        OriginalUrlData originalUrlData;

        try {
            originalUrlData = objectMapper.readValue(s3Object, OriginalUrlData.class);
        }catch (Exception e) {
            throw new RuntimeException("Error deserializing URL data: " + e.getMessage(), e);
        }

        long currentTimeInSeconds = System.currentTimeMillis()/1000;

        Map<String, Object> response = new HashMap<>();

        //Cenario onde a URL Expirou
        if (originalUrlData.getExpirationTime() < currentTimeInSeconds) {
            response.put("statusCode", 302);
            response.put("body", "This URL has expired.");
            return response;
        }

        //Cenario onde a URL esta valida e sera retornada para redirecionamento
        response.put("statusCode", 302);
        Map<String,String> headers = new HashMap<>();
        headers.put("Location", originalUrlData.getOriginalUrl());
        response.put("headers", headers);

        return response;
    }
}