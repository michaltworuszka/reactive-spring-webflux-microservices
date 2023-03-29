package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewsRestClient {

    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public Flux<Review> retrieveReviews(String movieId) {

        //movieInfoId
        String uri = UriComponentsBuilder.fromUriString(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {

                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {

                    log.info("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException("Server Exception in ReviewsService " + responseMessage)));
                })
                .bodyToFlux(Review.class)
                .log();
    }
}
