package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repo.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing") //this profile need to be different from other profiles used in Application
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    private static final String REVIEWS_URL = "/v1/reviews";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review("2", 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {

        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview(){

        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedReview);
                    assertNotNull(savedReview.getReviewId());

                });

    }

    @Test
    void updateReview(){

        var id = "/1";
        var review = new Review(null, 1L, "Awesome Movie2", 4.0);

        webTestClient
                .put()
                .uri(REVIEWS_URL + id)
                .bodyValue(review)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedReview);
                    assertEquals(review.getComment(), updatedReview.getComment());
                    assertEquals(review.getRating(), updatedReview.getRating());

                });
    }

    @Test
    void getAllReviews(){

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
    void deleteReview(){

        var id = "/1";

        webTestClient
                .delete()
                .uri(REVIEWS_URL + id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAllReviewsByMovieInfoId(){
        var param = "?movieInfoId=1";

        webTestClient
                .get()
                .uri(REVIEWS_URL + param)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void getAllReviewsByMovieInfoIdV2(){

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(REVIEWS_URL)
                        .queryParam("movieInfoId", "1")
                        .build())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void getAllReviewsByMovieInfoIdV3(){
        var uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                        .queryParam("movieInfoId", 1)
                                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }
}
