package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import de.ruu.lib.jpa.core.AbstractDTO;
import de.ruu.lib.jpa.core.AbstractEntity;
import de.ruu.lib.jpa.core.Entity;
import de.ruu.lib.jpa.core.mapstruct.AbstractMappedDTO;
import de.ruu.lib.util.Strings;
import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NodeDTO extends AbstractMappedDTO<NodeEntity> implements Node<NodeDTO>
{
	private static final Logger log = LoggerFactory.getLogger(NodeDTO.class);

	@NonNull  private String        name;
	@Nullable private NodeDTO       parent;
	@NonNull  private List<NodeDTO> children;

	protected NodeDTO() { } // required by mapstruct

	public NodeDTO(@NonNull String name)
	{
		name(name);
		children = new ArrayList<>();
	}
	public NodeDTO(@NonNull String name, @Nullable NodeDTO parent)
	{
		this(name);
		this.parent = parent;
	}

	@Override
	@NonNull
	public String name() { return name; }
	@NonNull
	public NodeDTO name(@NonNull String name)
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
	public Optional<NodeDTO> parent() { return Optional.ofNullable(parent); }
	@Override
	@NonNull
	public NodeDTO parent(@Nullable NodeDTO parent)
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
	@NonNull public List<NodeDTO> children() { return Collections.unmodifiableList(children); }

	/** handling of bidirectional relation */
	@Override
	public boolean add(@NonNull NodeDTO node)
	{
		node.parent(this);
		return children.add(node);
	}

	/** handling of bidirectional relation */
	@Override
	public boolean remove(@NonNull NodeDTO node)
	{
		boolean result = children.remove(node);
		if (result) node.parent(null);
		return result;
	}

	@Override
	public @NonNull NodeEntity toSource()
	{
		return MapperNodeEntityNodeDTO.INSTANCE.map(this);
	}

	protected void beforeMapping(@NonNull NodeEntity input) { super.beforeMapping(input); }
	protected void afterMapping (@NonNull NodeEntity input) { super.afterMapping (input); }

	public void beforeMapping(@NonNull NodeSimple source) { mapIdAndVersion(source); }

	private class AbstractEntitySimple extends AbstractEntity<NodeDTO>
	{
		private @NonNull NodeSimple nodeSimple;
		private AbstractEntitySimple(@NonNull NodeSimple nodeSimple) { this.nodeSimple = nodeSimple; }
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
		super.mapIdAndVersion(new AbstractEntitySimple(source));
	}
}
