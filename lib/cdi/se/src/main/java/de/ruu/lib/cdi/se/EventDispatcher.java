package de.ruu.lib.cdi.se;

import jakarta.enterprise.event.Observes;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Generic base class for CDI event observers that propagate received events to {@link Consumer}s.
 * <p>
 * If sub classes are singletons this allows to dispatch CDI events to <b>existing</b> consumers. This differs from
 * standard CDI @Observes behavior. Standard CDI behavior creates <b>new</b> instances for classes that have observer
 * methods.
 *
 * <pre>{@code
 * class SomeEvent
 * {
 * }
 *
 * @ApplicationScoped
 * class SomeEventDispatcher extends EventDispatcher<SomeEvent>
 * {
 * }
 *
 * class Client
 * {
 *   @Inject SomeEventDispatcher eventDispatcher;
 *   ...
 *   Client()
 *   {
 *     eventDispatcher.getConsumers().add(e -> consume(e);
 *   }
 *
 *   void consume(SomeEvent e)
 *   {
 *     ...
 *   }
 * }
 * }</pre>
 *
 * @param <E>
 *
 * @author r-uu
 */
public abstract class EventDispatcher<E>
{
	private Set<Consumer<E>> consumers = new LinkedHashSet<>();

	public boolean add(Consumer<E> consumer) { return consumers.add(consumer); }

	public boolean remove(Consumer<E> consumer) { return consumers.remove(consumer); }

	protected void observe(@Observes E event)
	{
//		log.debug
//		(
//				"{}.observe received {}, propagating to {} consumer(s)",
//				getClass().getName(), event.getClass().getName(), consumers.size()
//		);
		consumers.forEach(consumer -> consumer.accept(event));
	}
}