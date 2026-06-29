package com.motus.game.service;

import com.motus.game.dto.RandomWordResponse;
import com.motus.game.dto.WordValidationRequest;
import com.motus.game.dto.WordValidationResponse;
import com.motus.game.exception.DictServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class DictServiceClient {

    private final RestTemplate restTemplate;
    private final String dictServiceUrl;

    public DictServiceClient(RestTemplate restTemplate,
                              @Value("${motus.services.dict-service-url:http://localhost:8083}") String dictServiceUrl) {
        this.restTemplate = restTemplate;
        this.dictServiceUrl = dictServiceUrl;
    }

    public String getRandomWord(int length) {
        String url = dictServiceUrl + "/words/random?length=" + length;
        try {
            RandomWordResponse response = restTemplate.getForObject(url, RandomWordResponse.class);
            if (response == null || response.getWord() == null) {
                throw new DictServiceUnavailableException("dict-service returned an empty word");
            }
            return response.getWord().toUpperCase();
        } catch (RestClientException ex) {
            throw new DictServiceUnavailableException("dict-service is unavailable: " + ex.getMessage());
        }
    }

    public boolean isWordValid(String word) {
        String url = dictServiceUrl + "/words/validate";
        try {
            WordValidationResponse response = restTemplate.postForObject(
                    url, new WordValidationRequest(word), WordValidationResponse.class);
            return response != null && response.isValid();
        } catch (RestClientException ex) {
            throw new DictServiceUnavailableException("dict-service is unavailable: " + ex.getMessage());
        }
    }
}
