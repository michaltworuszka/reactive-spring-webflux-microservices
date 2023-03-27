package com.reactivespring.repo;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest //this will scan you app for repository classes
@ActiveProfiles("test")
class MoviesInfoRepoIntegrationTest {

    @Autowired
    MoviesInfoRepo moviesInfoRepo;

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")), new MovieInfo(null, "The Dark Knight",
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
    void findAll() {
        //given

        //when
        var movieInfoFlux = moviesInfoRepo.findAll().log();

        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        //given
        var movieNameExpected = "Dark Knight Rises";

        //when
        var movieInfoMono = moviesInfoRepo.findById("abc").log();

        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> assertEquals(movieNameExpected, movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        //given
        var movieInfoExpected = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        var movieInfoMono = moviesInfoRepo.save(movieInfoExpected).log();

        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(movieInfoExpected.getName(), movieInfo.getName());
                    assertNotNull(movieInfo.getMovieInfoId());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        //given
        var movieInfo = moviesInfoRepo.findById("abc").block();
        movieInfo.setYear(2021);

        //when
        var movieInfoMono = moviesInfoRepo.save(movieInfo).log();


        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> assertEquals(movieInfo.getYear(), movieInfo1.getYear()))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        //given

        //when
        moviesInfoRepo.deleteById("abc").block();
        Flux<MovieInfo> movieInfoFlux = moviesInfoRepo.findAll().log();

        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear() {
        //given
        int year = 2005;

        //when
        Flux<MovieInfo> movieInfoFlux = moviesInfoRepo.findByYear(year).log();

        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {
        //given
        String name = "Dark Knight Rises";

        //when
        Mono<MovieInfo> movieInfoMono = moviesInfoRepo.findByName(name).log();

        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(movieInfo.getName(), name);
                    assertEquals(movieInfo.getYear(), 2012);
                })
                .verifyComplete();
    }
}