package com.planmytrip.backend.repository;

import com.planmytrip.backend.model.Itinerary;
import com.planmytrip.backend.model.ItineraryDTO;
import com.planmytrip.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    @Query("SELECT new com.planmytrip.backend.model.ItineraryDTO(i.id, i.itinerary) FROM Itinerary i WHERE i.user = :user")
    List<ItineraryDTO> findItinerariesByUser(User user);
}
