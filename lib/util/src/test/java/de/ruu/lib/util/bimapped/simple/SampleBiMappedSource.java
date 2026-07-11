package de.ruu.lib.util.bimapped.simple;

import de.ruu.lib.util.bimapped.BiMap;
import de.ruu.lib.util.bimapped.BiMappedSource;
import org.jspecify.annotations.NonNull;

class SampleBiMappedSource implements BiMappedSource<SampleBiMappedTarget>
{
	@NonNull private BiMap biMap;
	@NonNull private String name;

	private SampleBiMappedSource() { biMap = new BiMap(); }

	public SampleBiMappedSource(@NonNull String name)
	{
		this();
		this.name = name;
	}

	public SampleBiMappedSource(@NonNull SampleBiMappedTarget target)
	{
		this.biMap = target.biMap();
		biMap.put(target, this);
		name = target.name();
	}

	@Override public @NonNull BiMap biMap() { return biMap; }

	public String name() { return name; }
}