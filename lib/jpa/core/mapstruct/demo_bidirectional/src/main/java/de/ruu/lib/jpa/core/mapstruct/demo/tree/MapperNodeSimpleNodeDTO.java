package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import de.ruu.lib.mapstruct.ReferenceCycleTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
abstract class MapperNodeSimpleNodeDTO
{
	private static final Logger log = LoggerFactory.getLogger(MapperNodeSimpleNodeDTO.class);

	static MapperNodeSimpleNodeDTO INSTANCE = Mappers.getMapper(MapperNodeSimpleNodeDTO.class);

	private final static ReferenceCycleTracking CONTEXT  = new ReferenceCycleTracking();

	// name and parent are handled by the objects themselves via beforeMapping callbacks
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "parent", ignore = true)
	abstract NodeDTO    map(NodeSimple input);
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "parent", ignore = true)
	abstract NodeSimple map(NodeDTO    input);

	/** annotating parameter {@code target} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping void beforeMapping(NodeSimple source, @MappingTarget NodeDTO target)
	{
		log.debug("before source {}, target  {}", source, target);
		target.beforeMapping(source); // invoke callback for mapping
	}

	/** annotating parameter {@code target} with {@link MappingTarget} is essential for this method being called */
	@BeforeMapping void beforeMapping(NodeDTO source, @MappingTarget NodeSimple target)
	{
		log.debug("before source {}, target  {}", source, target);
		target.beforeMapping(source); // invoke callback for mapping
	}
}