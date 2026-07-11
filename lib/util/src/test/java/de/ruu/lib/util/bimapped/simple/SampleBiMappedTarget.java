package de.ruu.lib.util.bimapped.simple;

import de.ruu.lib.util.bimapped.BiMap;
import de.ruu.lib.util.bimapped.BiMappedTarget;
import org.jspecify.annotations.NonNull;

class SampleBiMappedTarget implements BiMappedTarget<SampleBiMappedSource>
{
	@NonNull private BiMap biMap;
	@NonNull private String name;

	private SampleBiMappedTarget() { biMap = new BiMap(); }

	public SampleBiMappedTarget(@NonNull String name)
	{
		this();
		this.name = name;
	}

	public SampleBiMappedTarget(@NonNull SampleBiMappedSource source)
	{
		this.biMap = source.biMap();
		biMap.put(source, this);
		name = source.name();
	}

	@Override public @NonNull BiMap biMap() { return biMap; }

	public String name() { return name; }
}