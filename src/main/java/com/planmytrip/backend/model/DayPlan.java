package com.planmytrip.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DayPlan {
    private Integer day;
    private List<Activity> activities;

    @JsonCreator
    public DayPlan(@JsonProperty("day") int day,
                   @JsonProperty("activities") List<Activity> activities) {
        this.day = day;
        this.activities = activities;
    }
}
