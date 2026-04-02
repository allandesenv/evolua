package com.evolua.user.infrastructure.persistence;

import com.evolua.user.domain.Profile;
import com.evolua.user.domain.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class ProfilePersistenceAdapter implements ProfileRepository {
  private final ProfileJpaRepository repository;

  public ProfilePersistenceAdapter(ProfileJpaRepository repository) {
    this.repository = repository;
  }

  public Profile save(Profile item) {
    ProfileEntity entity = new ProfileEntity();
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setDisplayName(item.displayName());
    entity.setBio(item.bio());
    entity.setJourneyLevel(item.journeyLevel());
    entity.setPremium(item.premium());
    entity.setCreatedAt(item.createdAt());
    ProfileEntity saved = repository.save(entity);
    return new Profile(
        saved.getId(),
        saved.getUserId(),
        saved.getDisplayName(),
        saved.getBio(),
        saved.getJourneyLevel(),
        saved.getPremium(),
        saved.getCreatedAt());
  }

  public Page<Profile> findAllByUserId(String userId, Pageable pageable, String search, Boolean premium) {
    Specification<ProfileEntity> specification =
        Specification.where((root, query, cb) -> cb.equal(root.get("userId"), userId));

    if (search != null && !search.isBlank()) {
      var normalized = "%" + search.toLowerCase() + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("displayName")), normalized),
                      cb.like(cb.lower(root.get("bio")), normalized)));
    }

    if (premium != null) {
      specification = specification.and((root, query, cb) -> cb.equal(root.get("premium"), premium));
    }

    return repository.findAll(specification, pageable)
        .map(
            saved ->
                new Profile(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getDisplayName(),
                    saved.getBio(),
                    saved.getJourneyLevel(),
                    saved.getPremium(),
                    saved.getCreatedAt()));
  }
}
