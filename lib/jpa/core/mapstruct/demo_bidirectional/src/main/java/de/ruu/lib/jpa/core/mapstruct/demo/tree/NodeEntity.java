package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import de.ruu.lib.jpa.core.AbstractDTO;
import de.ruu.lib.jpa.core.mapstruct.AbstractMappedEntity;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is almost a complete copy of the code from {@link NodeSimple} or {@link AbstractMappedNode} just replacing generic type variables as T with concrete type like {@link NodeEntity} or {@link NodeDTO}.
 * TODO Is there a better way to go?
 * TODO Delegates perhaps?
 */
public class NodeEntity extends AbstractMappedEntity<NodeDTO> implements Node<NodeEntity>
{
	private static final Logger log = LoggerFactory.getLogger(NodeEntity.class);

	@NonNull  private String           name;
	@Nullable private NodeEntity       parent;
	@NonNull  private List<NodeEntity> children;

	protected NodeEntity() { } // required by mapstruct

	public NodeEntity(@NonNull String name)
	{
		name(name);
		children = new ArrayList<>();
	}

	public NodeEntity(@NonNull String name, @Nullable NodeEntity parent)
	{
		this(name);
		this.parent = parent;
	}

	@Override
	@NonNull
	public String name() { return name; }
	@NonNull
	public NodeEntity name(@NonNull String name)
	{
		if (Strings.isEmptyOrBlank(name)) throw new IllegalArgumentException("name must not be empty nor blank");
		this.name = name;
		return this;
	}

	/**
	 * Manually implement this method because lombok can not generate accessor returning {@code Optional} as required by
	 * {@link Node} for a non-{@code Optional} field as {@link #parent}.
	 *
	 * @return {@link #parent} wrapped in an {@code Optional}
	 */
	@Override
	@NonNull
	public Optional<NodeEntity> parent() { return Optional.ofNullable(parent); }
	@Override
	@NonNull
	public NodeEntity parent(@Nullable NodeEntity parent)
	{
		this.parent = parent;
		return this;
	}

	/**
	 * Manually implement this method because lombok can not generate accessor returning unmodifiable list.;
	 *
	 * @return {@link #parent} wrapped in an {@code Optional}
	 */
	@Override
	@NonNull public List<NodeEntity> children() { return Collections.unmodifiableList(children); }

	/** handling of bidirectional relation */
	@Override
	public boolean add(@NonNull NodeEntity node)
	{
		node.parent(this);
		return children.add(node);
	}

	/** handling of bidirectional relation */
	@Override
	public boolean remove(@NonNull NodeEntity node)
	{
		boolean result = children.remove(node);
		if (result) node.parent(null);
		return result;
	}

	@Override
	public @NonNull NodeDTO toTarget() { return MapperNodeEntityNodeDTO.INSTANCE.map(this); }

	protected void beforeMapping(@NonNull NodeSimple input) { super.beforeMapping(input); }
	protected void beforeMapping(@NonNull NodeDTO    input) { super.beforeMapping(input); }

	private class AbstractDTOSimple extends AbstractDTO<NodeEntity>
	{
		private @NonNull NodeSimple nodeSimple;
		private AbstractDTOSimple(@NonNull NodeSimple nodeSimple) { this.nodeSimple = nodeSimple; }
	}

	/**
	 * to be called in mapstruct callbacks by subclasses
	 * <p>
	 * Picks the {@link AbstractDTO#getId()} and {@link AbstractDTO#getVersion()} values and assigns them to the
	 * respective fields in this class.
	 *
	 * @param source
	 * @throws NullPointerException if {@code source} is {@code null}
	 */
	protected void mapIdAndVersion(@NonNull NodeSimple source)
	{
		// set fields that can not be modified from outside
		super.mapIdAndVersion(new AbstractDTOSimple(source));
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof NodeEntity other)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name); }

	@Override public String toString()
	{
		return super.toString() + ", NodeEntity(name=" + name + ")";
	}
}
