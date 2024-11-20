package com.java.curso.rockeseat.redirectUrlShortener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OriginalUrlData {
    private String originalUrl;
    private long expirationTime;
}
