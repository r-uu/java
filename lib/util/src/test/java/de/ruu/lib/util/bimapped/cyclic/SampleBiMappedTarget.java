package de.ruu.lib.util.bimapped.cyclic;

import de.ruu.lib.util.bimapped.BiMap;
import de.ruu.lib.util.bimapped.BiMappedTarget;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SampleBiMappedTarget implements BiMappedTarget<SampleBiMappedSource>
{
	@NonNull private BiMap biMap;
	@NonNull private String name;
	@NonNull private SampleBiMappedTarget predecessor;
	@NonNull private List<SampleBiMappedTarget> successors;

	private SampleBiMappedTarget()
	{
		biMap = new BiMap();
		predecessor = this;
		successors = new ArrayList<>();
	}

	SampleBiMappedTarget(@NonNull String name)
	{
		this();
		this.name = name;
	}

	SampleBiMappedTarget(@NonNull String name, @NonNull BiMap biMap)
	{
		this(name);
		this.biMap  = biMap;
	}

	public SampleBiMappedTarget(@NonNull String name, SampleBiMappedTarget predecessor)
	{
		this(name, predecessor.biMap());
		this.predecessor = predecessor;
	}

	public SampleBiMappedTarget(@NonNull SampleBiMappedSource source)
	{
		biMap       = source.biMap();
		name        = source.name();
		predecessor = source.predecessor().toTarget();
		successors  = new ArrayList<>();

		for (SampleBiMappedSource successor : source.successors())
		{
			successors.add(successor.toTarget());
		}

		biMap.put(source, this);
	}

	public SampleBiMappedSource toSource()
	{
		Optional<SampleBiMappedSource> optional = map(SampleBiMappedSource.class);

		if (optional.isPresent()) return optional.get();

		SampleBiMappedSource result = new SampleBiMappedSource(name, biMap);

		biMap.put(this, result);

		return result;
	}

	@Override public @NonNull BiMap biMap() { return biMap; }

	String name() { return name; }

	@NonNull SampleBiMappedTarget predecessor() { return predecessor; }

	@NonNull List<SampleBiMappedTarget> successors() { return successors; }
}