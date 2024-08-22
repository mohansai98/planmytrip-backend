package com.planmytrip.backend.controller;

import com.planmytrip.backend.model.ItineraryRequest;
import com.planmytrip.backend.model.ItineraryResponse;
import com.planmytrip.backend.service.ItineraryService;
import com.planmytrip.backend.util.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final JwtTokenUtil jwtTokenUtil;

    public ItineraryController(ItineraryService itineraryService, JwtTokenUtil jwtTokenUtil) {
        this.itineraryService = itineraryService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/generate")
    public ResponseEntity<ItineraryResponse> generateItinerary(@RequestBody ItineraryRequest request) {
        ItineraryResponse response = itineraryService.generateItinerary(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveItinerary(@RequestHeader("Authorization") String authorizationHeader, @RequestBody String data) {
        String token = null;
        String email = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtTokenUtil.extractUsername(token);
        }
        return itineraryService.saveItinerary(data, email);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getItineraries(@RequestHeader("Authorization") String authorizationHeader) {
        String token = null;
        String email = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtTokenUtil.extractUsername(token);
        }
        return itineraryService.getItineraries(email);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItinerary(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        String token = null;
        String email = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            email = jwtTokenUtil.extractUsername(token);
        }
        return itineraryService.deleteItinerary(email, id);
    }
}