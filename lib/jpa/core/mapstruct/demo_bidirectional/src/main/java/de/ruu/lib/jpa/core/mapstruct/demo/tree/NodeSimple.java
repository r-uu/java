package de.ruu.lib.jpa.core.mapstruct.demo.tree;

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

public class NodeSimple extends AbstractMappedNode<NodeDTO, NodeEntity> implements Node<NodeSimple>
{
	private static final Logger log = LoggerFactory.getLogger(NodeSimple.class);

	@NonNull  private String           name;
	@Nullable private NodeSimple       parent;
	@NonNull  private List<NodeSimple> children;

	protected NodeSimple() { } // required by mapstruct

	public NodeSimple(@NonNull String name)
	{
		name(name);
		children = new ArrayList<>();
	}

	public NodeSimple(@NonNull String name, @Nullable NodeSimple parent)
	{
		this(name);
		this.parent = parent;
	}

	@Override
	@NonNull
	public String name() { return name; }
	@NonNull
	public NodeSimple name(@NonNull String name)
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
	public Optional<NodeSimple> parent() { return Optional.ofNullable(parent); }
	@Override
	@NonNull
	public NodeSimple parent(@Nullable NodeSimple parent)
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
	@NonNull public List<NodeSimple> children() { return Collections.unmodifiableList(children); }

	/** handling of bidirectional relation */
	@Override
	public boolean add(@NonNull NodeSimple node)
	{
		node.parent(this);
		return children.add(node);
	}

	/** handling of bidirectional relation */
	@Override
	public boolean remove(@NonNull NodeSimple node)
	{
		boolean result = children.remove(node);
		if (result) node.parent(null);
		return result;
	}

	protected void beforeMapping(@NonNull NodeDTO    input) { super.beforeMapping(input); }
	protected void beforeMapping(@NonNull NodeEntity input) { super.beforeMapping(input); }

	@Override public @NonNull NodeDTO    toTarget() { return MapperNodeSimpleNodeDTO   .INSTANCE.map(this); }
	@Override public @NonNull NodeEntity toSource() { return MapperNodeSimpleNodeEntity.INSTANCE.map(this); }

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof NodeSimple other)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() { return Objects.hash(super.hashCode(), name); }

	@Override public String toString()
	{
		return super.toString() + ", NodeSimple(name=" + name + ")";
	}
}
