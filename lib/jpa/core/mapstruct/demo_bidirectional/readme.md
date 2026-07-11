Package ```de.ruu.lib.jpa.core.mapstruct.demo.tree``` demonstrates a case where an interface type ```Node``` is the central abstraction for modelling an acyclic graph called tree in this context. ```Node``` has an abstract implementation named ```NodeAbstract``` that implements the business logic of ```Node```s. ```NodeSimple``` extends ```NodeAbstract``` as a simple but complete implementation and is intended for general use.

However, ```NodeSimple``` data has to be persisted in a database and be transferred to remote processes. Therefore ```NodeSimple``` is accompanied by a corresponding ```NodeEntity``` for JPA persistence as well as a ````NodeDTO``` type for data transfer via JAX-RS. Mapstruct is used to create ```NodeEntity``` and ```NodeDTO``` instances from ```NodeSimple``` objects and vice versa.

```plantuml
@startuml

skinparam linetype ortho
' skinparam linetype polyline

'''''''
' types
'''''''

interface DTO<Entity>

interface Entity<DTO>
{
    Long  id()
    Short version()
}

interface Node<T extends Node>
{
    String      name();
    Optional<T> parent();
    List<T>     children();

    boolean add   (T node);
    boolean remove(T node);
}

abstract class AbstractDTO<AbstractEntity> implements Entity, DTO
abstract class AbstractEntity<AbstractDTO> implements Entity
{
    protected void mapIdAndVersion(D source)
}

interface BiMappedSource<T extends BiMappedTarget>
{
    void beforeMapping(T input);
    void afterMapping (T input);

	T toTarget();
}

interface BiMappedTarget<S extends BiMappedSource>
{
    void beforeMapping(S input); 
    void afterMapping (S input);

	S toSource();
}

abstract class AbstractMappedEntity extends AbstractEntity
abstract class AbstractMappedDTO    extends AbstractDTO

abstract class NodeAbstract<T extends NodeAbstract> implements Node<T>
{
    - String name
    - T parent
    - List<T> children
    + <<Create>> AbstractNode(String)
    + <<Create>> AbstractNode(String,T)
    + Optional<T> parent()
    + List<T> children()
    + boolean add(T)
    + boolean remove(T)
}

class NodeSimple extends NodeAbstract<NodeSimple>
{
    public NodeSimple(String name)                    { super(name); }
    public NodeSimple(String name, NodeSimple parent) { super(name, parent); }

    @Override public List<NodeSimple> children() { return super.children(); }
}

'''''''''''
' relations
'''''''''''

DTO         -left- Entity
AbstractDTO -left- AbstractEntity

BiMappedSource - BiMappedTarget

BiMappedTarget <|-- AbstractMappedDTO
BiMappedSource <|-- AbstractMappedEntity

@enduml
```