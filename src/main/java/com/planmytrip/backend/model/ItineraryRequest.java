package com.planmytrip.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryRequest {
    private String source;
    private String destination;
    private LocalDate fromDate;
    private LocalDate toDate;
}
