package com.planmytrip.backend.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.planmytrip.backend.model.Activity;
import com.planmytrip.backend.model.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GoogleApiService {

    private final GeoApiContext context;

    public GoogleApiService(@Value("${google.api.key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public void getPlaceDetails(Activity activity) {
        try {
            PlacesSearchResult[] results = PlacesApi.textSearchQuery(context, activity.getLocation()).await().results;
            if (results.length > 0) {
                activity.setPlaceId(results[0].placeId);
                Coordinates coordinates = new Coordinates(
                        results[0].geometry.location.lat,
                        results[0].geometry.location.lng
                );
                activity.setCoordinates(coordinates);
                activity.setLocation(results[0].formattedAddress);
            }
        } catch (Exception e) {
            System.out.println("Error fetching place details: "+ e.getLocalizedMessage());
        }
    }

}
