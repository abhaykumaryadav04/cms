package com.a4b.cms.event.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.event.dto.CreateEventRequest;
import com.a4b.cms.event.dto.EventResponse;
import com.a4b.cms.event.service.EventService;

import jakarta.validation.Valid;

@RestController 
public class EventController {
  private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
     @PostMapping("/api/v1/clubs/{clubId}/events")
    public ResponseEntity<EventResponse> createDraft(
            @PathVariable UUID clubId,
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

 EventResponse response = eventService.createDraft(clubId,userDetails.getUsername(),request );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
     @GetMapping("/api/v1/events")
    public ResponseEntity<Page<EventResponse>> getApprovedEvents(
            @RequestParam(required = false) UUID clubId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startsAfter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startsBefore,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(eventService.getApprovedEvent(clubId,startsAfter,startsBefore,search,page,size );
    }
   
    @GetMapping("/api/v1/events/{eventId}")
    public ResponseEntity<EventResponse> getApprovedEvent( @PathVariable UUID eventId) {

        return ResponseEntity.ok(eventService.getApprovedEvent(eventId) );
    }
}
