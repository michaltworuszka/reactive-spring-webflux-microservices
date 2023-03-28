package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exeptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repo.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        ReviewRouter.class,
        ReviewHandler.class,
        GlobalErrorHandler.class
})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    private static final String REVIEWS_URL = "/v1/reviews";

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie2", 4.0);

        //when
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("a1", 1L, "Awesome Movie2", 4.0)));

        //then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assertEquals(review.getComment(), savedReview.getComment());
                    assertEquals(review.getRating(), savedReview.getRating());

                });
    }

    @Test
    void updateReview() {
        //given
        var id = "a1";
        var existingReview = new Review(id, 1L, "Awesome Movie2", 9.0);
        var reviewUpdate = new Review(id, 1L, "Pretty good movie", 5.0);

        //when
        when(reviewReactiveRepository.findById(isA(String.class)))
                .thenReturn(Mono.just(existingReview));

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(reviewUpdate));

        //then
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/" + id)
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedReview);
                    assertEquals(reviewUpdate.getComment(), updatedReview.getComment());
                    assertEquals(reviewUpdate.getRating(), updatedReview.getRating());

                });
    }

    @Test
    void getAllReviews() {
        //given
        var reviewsList = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review("2", 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        var reviewFlux = Flux.fromIterable(reviewsList);

        //when
        when(reviewReactiveRepository.findAll()).thenReturn(reviewFlux);

        //then
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void deleteReview() {
        //given
        var id = "/1";
        var existingReview = new Review(id, 1L, "Awesome Movie2", 9.0);

        //when
        when(reviewReactiveRepository.findById(isA(String.class)))
                .thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());

        //then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAllReviewsByMovieInfoId() {
        //given
        var existingReview = new Review("a1", 1L, "Awesome Movie2", 9.0);

        //when
        when(reviewReactiveRepository.findReviewsByMovieInfoId(isA(Long.class)))
                .thenReturn(Flux.just(existingReview));

        //then
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(REVIEWS_URL)
                        .queryParam("movieInfoId", "1")
                        .build())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void addReview_validation() {
        //given
        var review = new Review(null, null, "Awesome Movie2", -4.0);

        //when
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("a1", 1L, "Awesome Movie2", 4.0)));

        //then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.negative : rating is negative and please pass a non-negative value, review.movieInfoId: must not be null");
    }
}
