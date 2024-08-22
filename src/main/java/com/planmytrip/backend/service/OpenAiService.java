package com.planmytrip.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planmytrip.backend.model.ItineraryRequest;
import com.planmytrip.backend.model.ItineraryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public OpenAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ItineraryResponse generateItinerary(ItineraryRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a detailed and accurate travel assistant, providing clear and precise travel itineraries."),
                        Map.of("role", "user", "content", createPrompt(request))
                ),
                "functions", List.of(createItineraryFunction()),
                "function_call", Map.of("name", "generate_itinerary"),
                "max_tokens", 16384
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(apiUrl, entity, String.class);
        return parseResponse(response);
    }

    private String createPrompt(ItineraryRequest request) {
        return String.format(
                "Generate a flexible travel itinerary for a trip from %s to %s. " +
                        "The trip starts on %s and ends on %s. " +
                        "Please provide a detailed day-by-day plan with activities, following this structure: " +
                        "[{ 'day': integer, 'activities': [{ 'name': string, 'description': string, 'duration': float (hours), 'location': string (full address), 'type': 'MORNING' | 'AFTERNOON' | 'EVENING' | 'FULL_DAY' }] }]. " +
                        "Ensure each activity has a clear name, a concise description, an estimated duration in hours (e.g., 2.5 hours), and a complete location including street, city, state, and zip code. " +
                        "If there are no activities for a part of the day, do not create placeholder activities. " +
                        "Also, indicate the part of the day when each activity should take place (morning, afternoon, evening, full day). " +
                        "Please adhere strictly to the JSON structure and avoid any unnecessary text.",
                request.getSource(), request.getDestination(),
                request.getFromDate(), request.getToDate()
        );
    }

    private Map<String, Object> createItineraryFunction() {
        return Map.of(
                "name", "generate_itinerary",
                "description", "Generate a flexible travel itinerary based on user inputs",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "itinerary", Map.of(
                                        "type", "array",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "day", Map.of("type", "integer"),
                                                        "activities", Map.of(
                                                                "type", "array",
                                                                "items", Map.of(
                                                                        "type", "object",
                                                                        "properties", Map.of(
                                                                                "name", Map.of("type", "string"),
                                                                                "description", Map.of("type", "string"),
                                                                                "duration", Map.of("type", "number"),
                                                                                "location", Map.of("type", "string"),
                                                                                "type", Map.of(
                                                                                        "type", "string",
                                                                                        "enum", List.of("MORNING", "AFTERNOON", "EVENING", "FULL_DAY")
                                                                                )
                                                                        ),
                                                                        "required", List.of("name", "description", "duration", "location", "type")
                                                                )
                                                        )
                                                ),
                                                "required", List.of("day", "activities")
                                        )
                                )
                        ),
                        "required", List.of("itinerary")
                )
        );
    }

    private ItineraryResponse parseResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode choicesNode = rootNode.path("choices").get(0);
            JsonNode functionCallNode = choicesNode.path("message").path("function_call");

            if (functionCallNode.isMissingNode()) {
                throw new RuntimeException("No function call found in the response");
            }

            String functionName = functionCallNode.path("name").asText();
            if (!"generate_itinerary".equals(functionName)) {
                throw new RuntimeException("Unexpected function call: " + functionName);
            }

            String argumentsJson = functionCallNode.path("arguments").asText();

            JsonNode argumentsNode = objectMapper.readTree(argumentsJson);
            if (!argumentsNode.has("itinerary")) {
                throw new RuntimeException("Invalid structure: missing 'itinerary'");
            }

            return objectMapper.readValue(argumentsJson, ItineraryResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }


}
