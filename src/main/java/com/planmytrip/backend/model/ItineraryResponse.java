package com.planmytrip.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class ItineraryResponse {
    private List<DayPlan> itinerary;
}
