/**
 * Note:
 * <p>
 * This javadoc should be contained in package-info.java. However there is a problem with package-info.java
 * that leads to {@link NoClassDefFoundError}s.
 * <p>
 * This <code>FXComp</code> package provides support for building large JavaFX applications from smaller visual
 * components.
 * <p>
 * The <code>FXComp</code> naming conventions support automation of recurring tasks when creating
 * visual components. For a visual component named X the conventions expect:
 * <ul>
 *   <li>component class name: <code>X</code></li>
 *   <li>component service interface name: <code>XService</code></li>
 *   <li>component controller class name: <code>XController</code></li>
 *   <li>application class name: <code>XApp</code></li>
 *   <li>application runner class name: <code>XAppRunner</code></li>
 * </ul>
 * These naming conventions can be overwritten in the classes described below.
 * <p>
 * <code>FXComp</code> provides the following base classes for the afore mentioned artifacts:
 * <ul>
 *   <li>component base class: {@link de.ruu.lib.fx.comp.DefaultFXCView}</li>
 *   <li>component controller base class: {@link de.ruu.lib.fx.comp.FXCController}</li>
 *   <li>component service interface name: {@link de.ruu.lib.fx.comp.FXCService}</li>
 *   <li>application base class: {@link de.ruu.lib.fx.comp.FXCApp}</li>
 *   <li>application runner base class: {@link de.ruu.lib.fx.comp.DefaultFXCView}</li>
 * </ul>
 * In addition to these base classes there are the following accompanying types.
 * <ul>
 *   <li>application started event class: {@link de.ruu.lib.fx.comp.FXCAppStartedEvent}</li>
 *   <li>application started event observer class: {@link de.ruu.lib.fx.comp.FXCAppStartedObserver}</li>
 * </ul>
 * <p>
 * See the documentation of the classes for a description of their purpose and features.
 */
package de.ruu.lib.fx.comp;

import javafx.scene.Parent;
import org.jspecify.annotations.NonNull;

/**
 * Interface for JavaFX visual components (<code>FXComp</code> views) that are designed to be used as building blocks
 * for bigger JavaFX visual components.
 * <p>
 * To facilitate easy integration in large JavaFX applications {@link FXCView} provides:
 * <ul>
 *   <li>Access to a node graph that defines the visual appearance of a view via {@link #localRoot()}. The visual
 *       appearance is usually defined in a <code>.fxml</code> file that contains a declarative description of the
 *       JavaFX components of the {@link FXCView} (buttons, textfields, labels, ...) and their layout.</li>
 *   <li>Access to the services of the component.</li>
 * </ul>
 *
 * @author r-uu
 */
public interface FXCView<S extends FXCService>
{
	/**
	 * @return {@link Parent} representing the root of the component's tree of nodes. The tree of nodes defines the
	 *         visual appearance of the component.
	 */
	@NonNull Parent localRoot();

	/** @return implementation of {@link FXCService} */
	@NonNull S service();
}