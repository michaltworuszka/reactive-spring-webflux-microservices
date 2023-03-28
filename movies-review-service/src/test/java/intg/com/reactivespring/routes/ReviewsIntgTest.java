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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") //this profile need to be different from other profiles used in Application
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    private static final String MOVIES_INFO_URL = "/v1/reviews";

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
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
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

        //then
    }

    @Test
    void updateReview(){
        //given
        var id = "/1";
        var review = new Review(null, 1L, "Awesome Movie2", 4.0);

        //when
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + id)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedReview);
                    assertEquals(review.getComment(), updatedReview.getComment());
                    assertEquals(review.getRating(), updatedReview.getRating());

                });

        //then
    }
}
