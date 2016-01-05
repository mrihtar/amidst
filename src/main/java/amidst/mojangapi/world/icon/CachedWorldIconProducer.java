package amidst.mojangapi.world.icon;

import java.util.Collections;
import java.util.List;

import amidst.documentation.ThreadSafe;
import amidst.fragment.Fragment;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;

@ThreadSafe
public abstract class CachedWorldIconProducer extends WorldIconProducer {
	protected final RecognisedVersion recognisedVersion;

	private final Object cacheLock = new Object();
	private volatile List<WorldIcon> cache;

	public CachedWorldIconProducer(RecognisedVersion recognisedVersion) {
		this.recognisedVersion = recognisedVersion;
	}

	@Override
	public void produce(CoordinatesInWorld corner, WorldIconConsumer consumer) {
		for (WorldIcon icon : getCache()) {
			if (icon.getCoordinates().isInBoundsOf(corner, Fragment.SIZE)) {
				consumer.consume(icon);
			}
		}
	}

	public List<WorldIcon> getWorldIcons() {
		return getCache();
	}

	public WorldIcon getFirstWorldIcon() {
		List<WorldIcon> cache = getCache();
		if (cache.isEmpty()) {
			return null;
		} else {
			return cache.get(0);
		}
	}

	public void resetCache() {
		cache = null;
	}

	/**
	 * Gets the list of WorldIcons. Returns the cache and creates it, if
	 * necessary. This will never return null. This also ensures that
	 * createCache is only called by one thread at a time.
	 */
	private List<WorldIcon> getCache() {
		List<WorldIcon> result = cache;
		if (result == null) {
			synchronized (cacheLock) {
				if (cache == null) {
					cache = createCache();
				}
				result = cache;
			}
		}
		return result;
	}

	/**
	 * Creates the list of WorldIcons. This will never return null.
	 */
	private List<WorldIcon> createCache() {
		List<WorldIcon> result = doCreateCache();
		if (result == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(result);
		}
	}

	/**
	 * This actually create the cache. This can return null. This will only be
	 * called by one thread at a time.
	 */
	protected abstract List<WorldIcon> doCreateCache();
}