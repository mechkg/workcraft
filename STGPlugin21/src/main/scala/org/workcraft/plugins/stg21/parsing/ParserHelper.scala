package org.workcraft.plugins.stg21.parsing
import org.workcraft.plugins.stg.DotGParserHelper
import org.workcraft.plugins.stg21.types.PlaceNode
import org.workcraft.plugins.stg21.types.StgNode
import org.workcraft.plugins.stg21.types.Place
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg.javacc.generated.ParseException
import org.workcraft.plugins.stg.Direction
import org.workcraft.plugins.stg.Type
import org.workcraft.plugins.stg21.fields.MathStgFields
import scalaz.State
import org.workcraft.plugins.stg21.StgOperations

class ParserHelper extends DotGParserHelper[PlaceNode, StgNode] {
  private var stg : MathStg = MathStg.empty
  private var nameToPlace : Map[String, PlaceNode] = Map.empty
  
  private def exec[T](st : State[MathStg, T]) : T = {
    val (newStg,x) = st(stg)
    stg = newStg
    x
  }
  
  @throws(classOf[ParseException])
  override def getPlace(name : String) : PlaceNode = {
    nameToPlace.getOrElse(name, {
      val newPlace = PlaceNode(exec(StgOperations.createMathPlace))
      nameToPlace = nameToPlace + (name -> newPlace)
      newPlace
    })
  }
  
  @throws(classOf[ParseException])
  override def getOrCreate (name : String) : StgNode = {
    _
  }
  
  @throws(classOf[ParseException])
  override def getOrCreate (ref : Pair[String, Integer]) : StgNode = {
    _
  }
  @throws(classOf[ParseException])
  override def getOrCreate (ref : Triple[String, Direction, Integer]) : StgNode = {
    _
  }
  @throws(classOf[ParseException])
  override def createArc (first : StgNode, second : StgNode) : Unit = {
    _
  }
  @throws(classOf[ParseException])
  override def setSignalsType (list : List[String], typ : Type) : Unit = {
    _
  }
  @throws(classOf[ParseException])
  override def getImplicitPlace(t1 : StgNode, t2 : StgNode) : PlaceNode = {
    _
  }
  
  override def setCapacity(p : PlaceNode, capacity : Int) : Unit = {
    _
  }
  override def setMarking(p : PlaceNode, capacity : Int) : Unit = {
    _
  }
}
