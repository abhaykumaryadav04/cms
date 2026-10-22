package com.a4b.cms.club.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import com.a4b.cms.club.dto.ClubResponse;
import com.a4b.cms.club.dto.CreateClubRequest;
import com.a4b.cms.club.entity.Club;
import com.a4b.cms.club.entity.ClubStatus;
import com.a4b.cms.club.repo.ClubRepo;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.user.entity.SystemRole;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service 
public class ClubService {

    private final ClubRepo clubRepo;
    private  final UserRepo userRepo;
    public  ClubService(ClubRepo clubRepo,UserRepo userRepo){
        this.userRepo=userRepo;
        this.clubRepo=clubRepo;
    }

    @Transactional 
    public ClubResponse createClub(String creatorEmail,CreateClubRequest request){
        User creator=userRepo.findByEmailIgnoreCase(creatorEmail).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "The requesting user was not found."
                ));
             if (creator.getSystemRole() != SystemRole.ADMIN) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Only administrators can create clubs."
            );
        } 
        String name=request.name().trim();
        String slug=generateSlug(name);
          if (clubRepo.existsBySlugIgnoreCase(slug)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "CLUB_SLUG_ALREADY_EXISTS",
                    "A club with this slug already exists."
            );
        }
        Club club=Club.builder().category(normalizeOptional(request.category()))
                                .description(normalizeOptional(request.description()))
                                .slug(slug)
                                .status(ClubStatus.ACTIVE)
                                .logoUrl(request.logoUrl())
                                .name(name)
                                .createdBy(creator)
                                .build();
        Club savedClub=clubRepo.save(club);
        return  toResponse(savedClub);

    }
    @Transactional (readOnly = true)
    public Page<ClubResponse> searchClub(  String search,
            String category,
            int page,
            int size){

  if (page < 0 || size < 1 || size > 50) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAGINATION",
                    "Page must be non-negative and size must be between 1 and 50."
            );
        }

        String normalizedSearch = normalizeOptional(search);
        String normalizedCategory = normalizeOptional(category);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                )
        );

        return clubRepo.searchClubs(
                        ClubStatus.ACTIVE,
                        normalizedSearch,
                        normalizedCategory,
                        pageable
                )
                .map(this::toResponse);
    }
      @Transactional(readOnly = true)
    public ClubResponse getActiveClub(UUID clubId) {

        Club club = clubRepo.findById(clubId)
                .filter(c -> c.getStatus() == ClubStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "CLUB_NOT_FOUND",
                        "The requested active club was not found."
                ));

        return toResponse(club);
    }
    private String generateSlug(String name) {

        String normalized = Normalizer.normalize(
                name,
                Normalizer.Form.NFD
        );

        normalized = normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (normalized.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_CLUB_NAME",
                    "A valid club name is required."
            );
        }

        return normalized;
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ClubResponse toResponse(Club club) {

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getSlug(),
                club.getDescription(),
                club.getCategory(),
                club.getLogoUrl(),
                club.getStatus(),
                club.getCreatedBy().getId(),
                club.getFacultyAdvisor() == null
                        ? null
                        : club.getFacultyAdvisor().getId(),
                club.getCreatedAt(),
                club.getUpdatedAt()
        );
    }

}
