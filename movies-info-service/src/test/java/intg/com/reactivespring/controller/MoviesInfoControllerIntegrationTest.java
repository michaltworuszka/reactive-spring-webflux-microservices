package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repo.MoviesInfoRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") //this profile need to be different from other profiles used in Application
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    private static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    MoviesInfoRepo moviesInfoRepo;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        moviesInfoRepo.saveAll(movieInfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        moviesInfoRepo.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());

                });

        //then
    }

    @Test
    void getAllMovieInfos() {

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMovieInfoById() {
        var id = "abc";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(responseBody);

                });

    }

    @Test
    void getAllMovieInfoById_NotFound() {
        var id = "defg";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void getAllMovieInfoByIdv2() {
        var id = "abc";

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo() {

        var id = "abc";
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));


        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertEquals(movieInfo.getName(), updatedMovieInfo.getName());

                });
    }

    @Test
    void deleteMovieInfo() {
        //given
        var id = "abc";

        //when
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        //then
    }

    @Test
    void updateMovieInfo_NotFound() {

        var id = "def";
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));


        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getAllMovieInfoByYear() {

        URI uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand().toUri();


        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }
}