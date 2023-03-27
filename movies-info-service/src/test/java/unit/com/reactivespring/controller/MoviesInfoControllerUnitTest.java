package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    private static final String MOVIES_INFO_URL = "/v1/movieinfos";
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService movieInfoServiceMock;

    @Test
    void getAllMoviesInfo() {
        //given
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")), new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        //when
        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

        //then
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
    void getMoviesInfoById() {
        //given
        var id = "abc";
        var movieInfo = Mono.just(new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        //when
        when(movieInfoServiceMock.getMovieInfoById(id)).thenReturn(movieInfo);

        //then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->
                {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals("Dark Knight Rises", responseBody.getName());
                });
    }

    @Test
    void addMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        //then
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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());

                });
    }

    @Test
    void updateMovieInfo() {
        //given
        var id = "abc";
        var movieInfo = new MovieInfo(null, "Batman Begins - AGAIN",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo(
                                        id, "Batman Begins - AGAIN",
                                        2005,
                                        List.of("Christian Bale", "Michael Cane"),
                                        LocalDate.parse("2005-06-15")))
                );

        //then
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
        when(movieInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        //then
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void addMovieInfo_validationBadRequest() {

        var movieInfo = new MovieInfo(null, "",
                -2000, List.of(""), LocalDate.parse("2005-06-15"));


        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {

                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("response body : " + responseBody);
                    var expectedErrorMessage = "movieInfo.cast must be present, movieInfo.name must be present, movieInfo.year must be positive value";
                    assert responseBody != null;
                    assertEquals(expectedErrorMessage, responseBody);

                });
    }

}