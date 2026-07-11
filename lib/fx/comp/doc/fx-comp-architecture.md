[back](../readme.md)

First of all - honour to whom honour is due!

Many concepts of the FX Comp architecture were initially inspired by [Adam Bien](https://github.com/AdamBien) and 
his [afterburner.fx](https://github.com/AdamBien/afterburner.fx) project. However, FX Comp pushes the limits of 
afterburner.fx by leveraging industry standards for CDI-based IOC and application event messaging.

# FX Comp Architecture - Conceptual Overview

The following diagram shows, in a somewhat simplified way, central ideas behind the FX Comp Architecture from a user's (i.e. a developer's) point of view.

An important design goal of FX Comp is to make development of complex user interfaces as simple and flexible as possible. At the same time, the complexity of even large applications is to be reduced by modularising and encapsulating the components.

To achieve this for FX Comp driven applications there are several ways for inter component communication. The diagram shows how tightly coupled components can use synchronous API calls or exchange events when they a loosely coupled. 

![FX Comp Architecture](fx-comp-architecture.png)

*conceptual overview of FX Comp collaboration*

## View

In FX Comp the **view** type is a Java class that can be used to compose complex user interfaces. It provides a handle to the FX node graph that represents the visual appearance of a component and a method to retrieve access to the component's service methods. The behaviour of the component is defined "behind the scenes" by a custom controller class (see below).

## Controller

The **controller** defines the behaviour of the component. It has access to all relevant visual controls. ```FXMLLoader``` achieves this by injecting values to controller fields annotated with ```@FXML``` at load time.

FX Comp internally uses CDI to bootstrap all the required parts (e.g. the above-mentioned service and the controller instances). Thereby CDI is globally available in a FX comp based application.

## Layout

FX Comp supports **layout configuration** of user interface components in .fxml files. Typically these are created and maintained using Gluon's excellent visual design tool [SceneBuilder](https://gluonhq.com/products/scene-builder/).

## Service

A component in FX Comp provides access to **custom services**. For example an editor component for a customer might provide a ```Customer getInitialCustomerValue()``` and a  ```Customer getModifiedCustomerValue()``` method. Service methods are usually defined in Java interfaces.

## App and AppRunner

FX Comp components are autonomous software building blocks that can be run independently in a JavaFX application. This allows for early user feedback regarding the visual design and eases testing the implementation of the component's behaviour. 

# FX Comp Architecture - A More Detailed Technical Overview

FX Comp components are structured into a couple of collaborating Java types. In the following the collaboration is described in more technical detail.

<!--![use url iuml](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/r-uu/r-uu.
lib/main/lib/fx/comp/doc/fx-comp-architecture-overview.puml)-->
![](fx-comp-architecture-overview.png)

*top level types for FX Comp*

The above diagrams shows the most important types that typically are used when creating FX Comp driven applications.

## FXCView

First of all there is ```FXCView```. Implementations of ```FXCView``` are the pivotal points in FX Comp driven applications. ```FXCView``` provides a ```javafx.scene.Parent``` that represents the layout of the visual component and at the same time automatically associates a custom ```FXCViewController``` that determines the components' behaviour.

The abstract default implementation ```DefaultFXCView``` automatically loads a layout configuration from a default .fxml file and automatically assigns a ```FXCViewController``` instance to determine the views behaviour. These mechanisms rely on "convention over configuration" but can be customised as necessary by implementations of ```DefaultFXCView```.

Extending the mentioned abstract view and controller classes with empty default implementations suffices to get a basic version of a custom FX Comp up and running (see in examples).

## FXCViewController

Implementations of FXCViewController are the working horses in a FX Comp driven application. ```DefaultFXCViewController``` is a good starting point for the definition of custom FX Comp components. Controller classes are where the behaviour and the collaboration with other FX Comp components is defined. Controller organize the collaboration with other FX View components either by directly calling their service methods or by producing and consuming events. However, controllers do not depend on other view's controllers. Instead, they only depend on the interfaces of the views they collaborate with.

# Runtime Environment

<!--![use url iuml](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/r-uu/r-uu.
lib/fx/comp/doc/fx-comp-runtime-environment.puml)-->
![](fx-comp-runtime-environment.png)

## FXCApp

This abstract class helps to create standalone JavaFX applications that run a single FX Comp component.

## FXCAppRunner

Bootstraps environment (CDI etc.) and runs ```FXCApp``` applications.

[FX Comp Demo](fx-comp-demo.md)

[back](../readme.md)