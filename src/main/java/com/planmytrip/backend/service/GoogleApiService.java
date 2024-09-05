package com.planmytrip.backend.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.Photo;
import com.google.maps.model.PlacesSearchResult;
import com.planmytrip.backend.model.Activity;
import com.planmytrip.backend.model.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


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
                Photo[] photos = results[0].photos;
                if (photos != null && photos.length > 0) {
                    Photo firstPhoto = photos[0];
                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?"
                            + "maxwidth=400"
                            + "&maxheight=300"
                            + "&photoreference=" + firstPhoto.photoReference
                            + "&key=AIzaSyC_ns2muhZYrlbsO5zyQR_x8jniRDuR7Ho";
                    activity.setPhotoUrl(photoUrl);

                }

            }
        } catch (Exception e) {
            System.out.println("Error fetching place details: " + e.getLocalizedMessage());
        }
    }

}