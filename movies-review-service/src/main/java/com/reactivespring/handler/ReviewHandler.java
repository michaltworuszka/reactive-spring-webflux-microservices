package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repo.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;

    private final Validator validator;

    public Mono<ServerResponse> addReview(@NotNull ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(savedReview));
    }

    private void validate(Review review) {

        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);
        if(constraintViolations.size() > 0) {
            String errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ReviewDataException(errorMessage);
        }
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

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not fout for the given reviewId : " + reviewId))); //1st approach to throw 404 not found

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
              //  .switchIfEmpty(ServerResponse.notFound().build()); //2nd approach to throw 404 not found
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
