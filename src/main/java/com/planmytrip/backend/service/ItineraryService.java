package com.planmytrip.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planmytrip.backend.model.*;
import com.planmytrip.backend.repository.ItineraryRepository;
import com.planmytrip.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ItineraryService {

    private final OpenAiService openAiService;
    private final GoogleApiService googleApiService;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;

    public ItineraryService(OpenAiService openAiService, GoogleApiService googleApiService, UserRepository userRepository, ItineraryRepository itineraryRepository) {
        this.openAiService = openAiService;
        this.googleApiService = googleApiService;
        this.userRepository = userRepository;
        this.itineraryRepository = itineraryRepository;
    }

    public ItineraryResponse generateItinerary(ItineraryRequest request) {
        ItineraryResponse response = openAiService.generateItinerary(request);
        return validateAndEnrichResponse(response);
    }

    private ItineraryResponse validateAndEnrichResponse(ItineraryResponse response) {
        for (DayPlan day : response.getItinerary()) {
            for (Activity activity : day.getActivities()) {
                googleApiService.getPlaceDetails(activity);
            }
        }
        return response;
    }

    public ResponseEntity<String> saveItinerary(String data, String email) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if(user == null) {
                return ResponseEntity.badRequest().body("Cannot validate user. Please login and try again");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(data);

            Itinerary itinerary = new Itinerary();
            itinerary.setItinerary(data);
            itinerary.setUser(user);
            itineraryRepository.save(itinerary);
            return ResponseEntity.ok("Itinerary Saved Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: "+e.getLocalizedMessage());
        }
    }

    public ResponseEntity<?> getItineraries(String email) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if(user == null) {
                return ResponseEntity.badRequest().body("Cannot validate user. Please login and try again");
            }
            List<ItineraryDTO> itineraryList = itineraryRepository.findItinerariesByUser(user);
            return ResponseEntity.ok(itineraryList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    public ResponseEntity<?> deleteItinerary(String email, Long id) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if(user == null) {
                return ResponseEntity.badRequest().body("Cannot validate user. Please login and try again");
            }
            itineraryRepository.deleteById(id);
            return ResponseEntity.ok().body("Itinerary deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }
}
