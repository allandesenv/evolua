package com.evolua.social.interfaces.rest;

import com.evolua.social.domain.Community;
import org.springframework.stereotype.Component;

@Component
public class CommunityMapper {
  public CommunityResponse toResponse(Community item, String currentUserId) {
    return new CommunityResponse(
        item.id(),
        item.slug(),
        item.name(),
        item.description(),
        item.visibility(),
        item.category(),
        item.memberIds().size(),
        item.memberIds().contains(currentUserId),
        item.createdAt());
  }
}
