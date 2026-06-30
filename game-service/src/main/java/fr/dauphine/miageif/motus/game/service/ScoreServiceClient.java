package fr.dauphine.miageif.motus.game.service;

import fr.dauphine.miageif.motus.game.dto.ScoreResultRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ScoreServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ScoreServiceClient.class);

    private final RestTemplate restTemplate;
    private final String scoreServiceUrl;

    public ScoreServiceClient(RestTemplate restTemplate,
                               @Value("${motus.services.score-service-url:http://localhost:8084}") String scoreServiceUrl) {
        this.restTemplate = restTemplate;
        this.scoreServiceUrl = scoreServiceUrl;
    }

    public void sendResult(ScoreResultRequest result) {
        String url = scoreServiceUrl + "/scores/results";
        try {
            restTemplate.postForLocation(url, result);
        } catch (RestClientException ex) {
            log.warn("Unable to send game result to score-service for gameId={}: {}", result.getGameId(), ex.getMessage());
        }
    }
}
