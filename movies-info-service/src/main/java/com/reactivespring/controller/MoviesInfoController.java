package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class MoviesInfoController {

    private final MoviesInfoService moviesInfoService;

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year){

        log.info("Year is : {}", year);

        if (year != null) {
            return moviesInfoService.getMovieInfoByYear(year).log();
        }

        return moviesInfoService.getAllMovieInfos().log();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable("id") String id) {

        return moviesInfoService.getMovieInfoById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo (@RequestBody @Valid MovieInfo movieInfo){

        return moviesInfoService.addMovieInfo(movieInfo).log();
    }


    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo (@RequestBody MovieInfo updatedMovieInfo, @PathVariable("id") String id){

        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void>deleteMovieInfo(@PathVariable("id") String id) {

        return moviesInfoService.deleteMovieInfo(id).log();

    }

}
