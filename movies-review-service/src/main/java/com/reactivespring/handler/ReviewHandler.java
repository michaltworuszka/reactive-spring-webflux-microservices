package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repo.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;
    public Mono<ServerResponse> addReview(@NotNull ServerRequest request) {

        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(savedRaview -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(savedRaview));
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        Flux<Review> reviews = reviewReactiveRepository.findAll();
        return ServerResponse.ok().body(reviews, Review.class);
    }
}
