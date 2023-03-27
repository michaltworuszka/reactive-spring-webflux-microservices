package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document //mongo annotation
public class MovieInfo {

    @Id
    private String movieInfoId;
    @NotBlank(message = "movieInfo.name must be present")
    private String name;
    @Positive(message = "movieInfo.year must be positive value")
    @NotNull
    private Integer year;

    private List<@NotBlank(message = "movieInfo.cast must be present") String> cast;
    private LocalDate releaseDate;
}
