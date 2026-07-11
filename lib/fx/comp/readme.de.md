# FX Comp

FX Comp is a software architecture proposal for building complex Java FX applications. It provides a software 
framework that supports implementing this architecture and comes with tools for generating artifacts that adhere to it.

To explain some of the challenges of developing bigger applications with Java FX here is a brief overview of how Java FX works.

Java FX is a UI framework that supports comfortable design of feature rich user interfaces with a visual design tool named SceneBuilder. SceneBuilder is a WYSIWYG editor that produces configuration files (.fxml) for load-time creation of an application's UI layout.

In Java FX application logic can be decoupled strictly from layout. A so called ```FXMLLoader``` can be configured to use a custom controller object. Then at load time ```FXMLLoader``` automatically injects references to relevant UI controls into the controller. A controller is an instance of a regular Java class with annotated fields for each relevant UI control.

To make this work it is necessary to tell the loader which controls shall be bound to which controller fields. This is easily done by giving a unique name to each relevant UI control in the visual designer and by making sure the controller class has an equally named annotated field for each of the controls.
