package com.planmytrip.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private String name;
    private String description;
    private float duration;
    private String location;
    private String placeId;
    private Coordinates coordinates;
    private TimeOfDay type;
    private String photoUrl;

    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING, FULL_DAY
    }
}