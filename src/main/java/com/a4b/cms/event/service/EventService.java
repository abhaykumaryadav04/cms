package com.a4b.cms.event.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.club.entity.Club;
import com.a4b.cms.club.entity.ClubStatus;
import com.a4b.cms.club.repo.ClubRepo;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.event.dto.CreateEventRequest;
import com.a4b.cms.event.dto.EventResponse;
import com.a4b.cms.event.entity.Event;
import com.a4b.cms.event.entity.EventStatus;
import com.a4b.cms.event.repo.EventRepo;
import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;
import com.a4b.cms.membership.repo.ClubMembershipRepo;
import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service 
public class EventService {
    private final EventRepo eventRepository;
    private final ClubRepo clubRepository;
    private final UserRepo userRepository;
    private final ClubMembershipRepo membershipRepository;

    public EventService(
            EventRepo eventRepository,
            ClubRepo clubRepository,
            UserRepo userRepository,
            ClubMembershipRepo membershipRepository
    ) {
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }
  @Transactional 
  public EventResponse createDraft( UUID clubId,
            String email,
            CreateEventRequest request){
                
  User creator=findUser(email);
  if (creator.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ApiException( HttpStatus.FORBIDDEN,
                    "ACCOUNT_DISABLED", "A disabled account cannot create events.");
        }
    Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ApiException( HttpStatus.NOT_FOUND,
                        "CLUB_NOT_FOUND", "The requested club was not found." ));
  
   if (club.getStatus() != ClubStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "CLUB_NOT_ACTIVE", "Events cannot be created for an archived club.");
        }
         boolean canManageEvents =membershipRepository.existsByClub_IdAndUser_IdAndStatusAndClubRoleIn(
                                clubId,creator.getId(),MembershipStatus.ACTIVE,
                                 List.of( ClubRole.PRESIDENT, ClubRole.SECRETARY ) );
        if (!canManageEvents) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "CLUB_EVENT_ACCESS_DENIED",
                    "Only the President or Secretary of this club can create events.");
        }
         validateEventDates(request);
         Event event=Event.builder().club(club)
                                     .createdBy(creator)
                                     .title(request.title().trim())
                                     .description(normalizeOptional(request.description()))
                                     .venue(request.venue().trim())
                                     .startsAt(request.startsAt())
                                     .endsAt(request.endsAt())
                                     .registrationDeadline(request.registrationDeadline())
                                     .capacity(request.capacity())
                                     .status(EventStatus.DRAFT)
                                     .build();
        Event savedEvent=eventRepository.save(event);
        return toResponse(savedEvent);
    }
    @Transactional (readOnly = true)
    public  Page<EventResponse> getApprovedEvent( UUID clubId,
            Instant startsAfter,Instant startsBefore,String search, int page, int size){
                 validatePagination(page, size);
        if (startsAfter != null && startsBefore != null && startsAfter.isAfter(startsBefore)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DATE_RANGE",
                    "The start of the search range must not be after its end." );
        }
         String normalizedSearch = normalizeOptional(search);
         PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by( Sort.Direction.ASC,"startsAt" ) );

        return eventRepository.searchApprovedEvents(
                        EventStatus.APPROVED,
                        clubId,
                        startsAfter,
                        startsBefore,
                        normalizedSearch,
                        pageable).map(this::toResponse);
            }
     @Transactional(readOnly = true)
    public EventResponse getApprovedEvent(UUID eventId) {

        Event event = eventRepository
                .findByIdAndStatus(
                        eventId,
                        EventStatus.APPROVED
                )
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND",
                        "The approved event was not found."
                ));

        return toResponse(event);
    }

  private User findUser(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"USER_NOT_FOUND", "The user account was not found."));
    }
    private void validateEventDates(CreateEventRequest request) {

        Instant now = Instant.now();

        if (!request.startsAt().isAfter(now)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_START",
                    "Event start time must be in the future."
            );
        }

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_DATES",
                    "Event end time must be after its start time."
            );
        }

        if (!request.registrationDeadline().isAfter(now)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REGISTRATION_DEADLINE",
                    "Registration deadline must be in the future."
            );
        }

        if (request.registrationDeadline().isAfter(
                request.startsAt()
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_REGISTRATION_DEADLINE",
                    "Registration deadline cannot be after the event starts."
            );
        }
    }

    private void validatePagination(int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAGINATION",
                    "Page must be non-negative and size must be between 1 and 50."
            );
        }
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private EventResponse toResponse(Event event) {

        return new EventResponse(
                event.getId(),

                event.getClub().getId(),
                event.getClub().getName(),

                event.getCreatedBy().getId(),
                event.getCreatedBy().getFullName(),

                event.getTitle(),
                event.getDescription(),
                event.getVenue(),

                event.getStartsAt(),
                event.getEndsAt(),

                event.getCapacity(),
                event.getRegistrationDeadline(),

                event.getStatus(),

                event.getApprovedBy() == null
                        ? null
                        : event.getApprovedBy().getId(),

                event.getApprovedAt(),

                event.getCancellationReason(),

                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
