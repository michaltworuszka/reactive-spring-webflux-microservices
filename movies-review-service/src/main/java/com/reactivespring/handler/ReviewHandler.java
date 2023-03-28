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

import java.util.Optional;

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

    public Mono<ServerResponse> getReviews(@NotNull ServerRequest request) {

        Optional<String> movieInfoId = request.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            Flux<Review> reviewsByMovieInfoId = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get())).log();
            return buildReviewsResponse(reviewsByMovieInfoId);

        } else {
            Flux<Review> reviews = reviewReactiveRepository.findAll().log();
            return buildReviewsResponse(reviews);
        }
    }

    private @NotNull Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsByMovieInfoId) {
        return ServerResponse.ok().body(reviewsByMovieInfoId, Review.class);
    }

    public Mono<ServerResponse> updateReview(@NotNull ServerRequest request) {

        String reviewId = request.pathVariable("id");

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(@NotNull ServerRequest request) {
        String reviewId = request.pathVariable("id");

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview.flatMap(review ->
                reviewReactiveRepository
                        .deleteById(reviewId)
                        .then(ServerResponse.noContent().build()));
    }
}
