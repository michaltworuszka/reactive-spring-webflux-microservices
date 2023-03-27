package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repo.MoviesInfoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MoviesInfoService {

    private final MoviesInfoRepo moviesInfoRepo;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
       return moviesInfoRepo.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovieInfos() {
        return moviesInfoRepo.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return moviesInfoRepo.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
        return moviesInfoRepo.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    return moviesInfoRepo.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return moviesInfoRepo.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {

        return moviesInfoRepo.findByYear(year);
    }
}
