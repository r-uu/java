package de.ruu.lib.util.bimapped.cyclic;

import de.ruu.lib.util.bimapped.BiMap;
import de.ruu.lib.util.bimapped.BiMappedSource;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SampleBiMappedSource implements BiMappedSource<SampleBiMappedTarget>
{
	@NonNull private BiMap biMap;
	@NonNull private String name;
	@NonNull private SampleBiMappedSource predecessor;
	@NonNull private List<SampleBiMappedSource> successors;

	private SampleBiMappedSource()
	{
		biMap       = new BiMap();
		predecessor = this;
		successors  = new ArrayList<>();
	}

	SampleBiMappedSource(@NonNull String name)
	{
		this();
		this.name = name;
	}

	SampleBiMappedSource(@NonNull String name, @NonNull BiMap biMap)
	{
		this(name);
		this.biMap  = biMap;
	}

	public SampleBiMappedSource(@NonNull String name, @NonNull SampleBiMappedSource predecessor)
	{
		this(name, predecessor.biMap());
		this.predecessor = predecessor;
	}

	public SampleBiMappedSource(@NonNull SampleBiMappedTarget target)
	{
		biMap       = target.biMap();
		name        = target.name();
		predecessor = target.predecessor().toSource();
		successors  = new ArrayList<>();

		for (SampleBiMappedTarget successor : target.successors())
		{
			successors.add(successor.toSource());
		}

		biMap.put(target, this);
	}

	public SampleBiMappedTarget toTarget()
	{
		Optional<SampleBiMappedTarget> optional = map(SampleBiMappedTarget.class);

		if (optional.isPresent()) return optional.get();

		SampleBiMappedTarget result = new SampleBiMappedTarget(name, biMap);

		biMap.put(this, result);

		return result;
	}

	@Override public @NonNull BiMap biMap() { return biMap; }

	public String name() { return name; }

	SampleBiMappedSource predecessor() { return predecessor; }

	@NonNull List<SampleBiMappedSource> successors() { return successors; }
}