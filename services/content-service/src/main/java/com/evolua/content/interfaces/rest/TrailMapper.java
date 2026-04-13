package com.evolua.content.interfaces.rest;

import com.evolua.content.domain.Trail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrailMapper {
  default TrailResponse toResponse(Trail item) {
    return new TrailResponse(
        item.id(),
        item.userId(),
        item.title(),
        item.summary(),
        item.content(),
        item.category(),
        item.premium(),
        item.privateTrail(),
        item.activeJourney(),
        item.generatedByAi(),
        item.journeyKey(),
        item.sourceStyle(),
        true,
        item.mediaLinks().stream()
            .map(link -> new TrailMediaLinkResponse(link.label(), link.url(), link.type()))
            .toList(),
        item.createdAt());
  }
}
