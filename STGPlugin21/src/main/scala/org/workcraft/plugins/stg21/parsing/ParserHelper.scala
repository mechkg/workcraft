package org.workcraft.plugins.stg21.parsing
import org.workcraft.plugins.stg.DotGParserHelper
import org.workcraft.plugins.stg21.types.StgNode
import org.workcraft.plugins.stg21.types.ExplicitPlace
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg.javacc.generated.ParseException
import org.workcraft.plugins.stg.Direction
import org.workcraft.plugins.stg.Type
import org.workcraft.plugins.stg21.fields.MathStgLenses
import scalaz.State
import org.workcraft.plugins.stg21.StgOperations
import org.workcraft.plugins.stg21.types.ExplicitPlace
import org.workcraft.plugins.stg21.types.ImplicitPlaceArc
import org.workcraft.plugins.stg21.types.Id
import org.workcraft.plugins.stg21.types.Col
import org.workcraft.plugins.stg21.StateExtensions._
import org.workcraft.plugins.stg21.types.Transition
import org.workcraft.plugins.stg21.types.ExplicitPlaceNode
import org.workcraft.plugins.stg21.types.TransitionNode
import org.workcraft.plugins.stg21.types.DummyLabel
import org.workcraft.plugins.stg21.types.SignalLabel
import org.workcraft.plugins.stg21.types.TransitionDirection
import org.workcraft.plugins.stg21.types.Arc
import org.workcraft.plugins.stg21.types.Signal
import org.workcraft.plugins.stg21.types.SignalType
import scala.collection.JavaConversions._
import org.workcraft.plugins.stg21.types.TransitionLabel

sealed trait Place
case class ExplicitPlacePlace (p : Id[ExplicitPlace]) extends Place
case class ImplicitPlace (a : Id[ImplicitPlaceArc]) extends Place

class ParserHelper extends DotGParserHelper[Place, StgNode] {
  private var stg : MathStg = MathStg.empty
  private var nameToPlace : Map[String, Id[ExplicitPlace]] = Map.empty
  private var dummies : Set[String] = Set.empty
  private var signals : Map[String, Id[Signal]] = Map.empty
  private var transitions : Map[Transition,Id[Transition]] = Map.empty
  private var implicits : Map[(Id[Transition], Id[Transition]), Id[Arc]] = Map.empty
  
  def getStg : MathStg = stg
  
  private def exec[T](st : State[MathStg, T]) : T = {
    val (newStg,x) = st(stg)
    stg = newStg
    x
  }
  
  @throws(classOf[ParseException])
  override def getExplicitPlace(name : String) : Place = {
    ExplicitPlacePlace(nameToPlace.getOrElse(name, throwException("Place not found: " + name)))
  }
  
  private def getOrCreateExplicitPlace(name : String) : Id[ExplicitPlace] = {
    nameToPlace.getOrElse(name, {
      val newPlace = exec(StgOperations.createMathPlace(name))
      nameToPlace = nameToPlace + (name -> newPlace)
      newPlace
    })
  } 
  
  @throws(classOf[ParseException])
  private def throwException(msg : String) : Nothing = {
    throw new RuntimeException("This is supposed to be parse exception with a line/column info: " + msg)
  }
  
  @throws(classOf[ParseException])
  private def checkDummy(name : String) : Unit = {
    if(!dummies.contains(name)) throwException("Unknown dummy transition: " + name)
  }
  
  @throws(classOf[ParseException])
  private def getOrCreateTransition(trans : Transition) : Id[Transition] = {
    transitions.get(trans) match {
      case None => {
        val id = exec(StgOperations.createMathTransition(trans))
        transitions = transitions + ((trans, id))
        id
      }
      case Some(id) => id
    }
  }
  
  private def getOrCreateTransition(lbl : TransitionLabel) : Id[Transition] = getOrCreateTransition(lbl,0)
  
  @throws(classOf[ParseException])
  override def getOrCreate (name : String) : StgNode = {
    dummies.contains(name) match {
      case false => ExplicitPlaceNode(getOrCreateExplicitPlace(name))
      case true => TransitionNode(getOrCreateTransition(DummyLabel(name)))
    }
  }
  
  @throws(classOf[ParseException])
  override def getOrCreate (ref : org.workcraft.util.Pair[String, Integer]) : StgNode = {
    TransitionNode(getOrCreateTransition(DummyLabel(ref.getFirst), ref.getSecond))
  }
  
  private def convertDirection(dir : Direction) : TransitionDirection = {
    dir match {
      case Direction.PLUS => TransitionDirection.Rise
      case Direction.MINUS => TransitionDirection.Fall
      case Direction.TOGGLE => TransitionDirection.Toggle
    }
  }
  
  private def getSignal(name : String) = signals.get(name).getOrElse(throwException("Unknown signal: " + name))
  
  def defaultZero(x : Integer) : Int = {
    if(x==null) 0
    else x
  }
  
  @throws(classOf[ParseException])
  override def getOrCreate (ref : org.workcraft.util.Triple[String, Direction, Integer]) : StgNode = {
    var signalId = getSignal(ref.getFirst)
    TransitionNode(getOrCreateTransition(SignalLabel(signalId, convertDirection(ref.getSecond)), defaultZero(ref.getThird)))
  }
  @throws(classOf[ParseException])
  override def createArc (first : StgNode, second : StgNode) : Unit = {
    (first, second) match {
      case (TransitionNode(t1), TransitionNode(t2)) => {
        val implicitPlaceArc = exec(StgOperations.connectTransitions(t1, t2))
        implicits = implicits + (((t1, t2), implicitPlaceArc))
      }
      case (ExplicitPlaceNode(p), TransitionNode(t)) => exec(StgOperations.connectPT(p, t))
      case (TransitionNode(t), ExplicitPlaceNode(p)) => exec(StgOperations.connectTP(t, p))
      case (ExplicitPlaceNode(p1), ExplicitPlaceNode(p2)) => throwException("Can't have an arc between places")
    }
  }
  
  private def setDummy(name : String) {
    dummies = dummies + name
  }
  
  private def setSigType(name : String, typ : SignalType) {
    signals.get(name) match {
      case Some(_) => throwException("duplicate signal spec: " + name)
      case None => {
        val sigId = exec(Col.add(Signal(name, typ)) on MathStg.signals)
        signals = signals + ((name, sigId))
      }
    }
  }

  @throws(classOf[ParseException])
  override def setSignalsType (list : java.util.List[String], typ : Type) : Unit = setSignalsTypeS(list.toList, typ)

  @throws(classOf[ParseException])
  private def setSignalsTypeS (list : List[String], typ : Type) : Unit = {
    for(s <- list) {
      typ match {
        case Type.DUMMY => setDummy(s)
        case Type.INPUT => setSigType(s, SignalType.Input)
        case Type.OUTPUT => setSigType(s, SignalType.Output)
        case Type.INTERNAL => setSigType(s, SignalType.Internal)
      }
    }
  }
  @throws(classOf[ParseException])
  override def getImplicitPlace(n1 : StgNode, n2 : StgNode) : Place = {
    (n1, n2) match {
      case (TransitionNode(t1), TransitionNode(t2)) => ImplicitPlace(implicits.get((t1, t2)).getOrElse(throwException("implicit place not found")).downCast)
      case _ => throwException("impossibel!")
    }
  }
  
  override def setCapacity(p : Place, capacity : Int) : Unit = {
    throw new RuntimeException("Capacity not supported");
  }
  override def setMarking(p : Place, marking : Int) : Unit = {
    exec(p match {
      case ExplicitPlacePlace(p) => Col.update(p)(pp => ExplicitPlace(marking, pp.name)).on(MathStg.places)
      case ImplicitPlace(p) => Col.update[Arc](p.upCast)(pp => pp match {
        case ImplicitPlaceArc(t1, t2, _) => null
        case _ => throw new RuntimeException("found some other arc where an implicit place arc was expected")
      }).on(MathStg.arcs)
      }
    )
  }
}
