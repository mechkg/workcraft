package org.workcraft.plugins.stg21
import org.workcraft.plugins.stg21.types.VisualNode
import org.workcraft.plugins.stg21.types.GroupVisualNode
import org.workcraft.graphics.Shape
import org.workcraft.gui.propertyeditor.EditableProperty
import scalaz.Lens
import org.workcraft.plugins.stg21.types.VisualStg
import org.workcraft.plugins.stg21.fields.VisualModelLenses
import org.workcraft.plugins.stg21.types.Group
import org.workcraft.plugins.stg21.types.Id
import org.workcraft.plugins.stg21.fields.VisualStgLenses
import org.workcraft.plugins.stg21.types.Col
import org.workcraft.plugins.stg21.EditorUIs._
import org.workcraft.plugins.stg21.modifiable._
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.plugins.stg21.fields.VisualInfoLenses
import org.workcraft.plugins.stg21.fields.GroupLenses
import org.workcraft.plugins.stg21.fields.Point2DLenses
import org.workcraft.plugins.stg21.types.VisualEntity
import org.workcraft.plugins.stg21.types.ArcVisualEntity
import org.workcraft.plugins.stg21.types.NodeVisualEntity
import org.workcraft.plugins.stg21.types.StgVisualNode
import org.workcraft.plugins.stg21.types.TransitionNode
import org.workcraft.plugins.stg21.types.ExplicitPlaceNode
import org.workcraft.plugins.stg21.types.VisualModel
import org.workcraft.plugins.stg21.types.VisualInfo
import org.workcraft.plugins.stg21.types.StgNode
import org.workcraft.plugins.stg21.types.VisualArc
import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.RelativePoint
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty
import org.workcraft.plugins.stg21.types.ExplicitPlace
import org.workcraft.plugins.stg21.types.MathStg
import org.workcraft.plugins.stg21.types.TransitionDirection
import org.workcraft.plugins.stg21.types.SignalType
import org.workcraft.plugins.stg21.types.Transition
import org.workcraft.scala.Expressions.Expression
import org.workcraft.scala.Expressions
import org.workcraft.scala.Scalaz._
import org.workcraft.plugins.stg21.types.DummyLabel
import org.workcraft.plugins.stg21.types.SignalLabel
import scalaz.Scalaz.modify
import scalaz.Scalaz.state
import org.workcraft.plugins.stg21.types.Signal
import scalaz.State
import org.workcraft.plugins.stg21.types.TransitionLabel
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.Bezier

object EditableProperties {
  
  type VisualStgEditableProperty[T] = Lens[VisualStg, T]
  type EditablePropertiesOf[T] = ModifiableExpression[T] => Expression[List[Expression[EditableProperty]]]
  
  implicit def decorateEditablePropertyOf[T](self : EditablePropertiesOf[T]) = new {
    def on[Q](lens : Lens[Q,T]) : EditablePropertiesOf[Q] = q => self(q.refract(lens))
    def ++(other : EditablePropertiesOf[T]) = (ex : ModifiableExpression[T]) => for(a <- self(ex); b <- other(ex)) yield (a++b)
  }
  
  def group (groupId : Id[Group]) : Lens[VisualStg, Group] =
     Col.uncheckedLook(groupId) compose (VisualModel.groups compose VisualStg.visual)
  
  def lensToEditableProperty[W,P] (lens : Lens[W,P], name : String)(implicit ui : EditorUI[P]) : EditablePropertiesOf[W] =
    w => Expressions.constant(List(ui.create(name)(w.refract(lens))))
  
  def visualInfoProps : EditablePropertiesOf[VisualInfo] = 
        lensToEditableProperty(Point2DLenses.x.compose(VisualInfo.position), "X") ++
        lensToEditableProperty(Point2DLenses.y.compose(VisualInfo.position), "Y")

  def groupProps : Id[Group] => EditablePropertiesOf[VisualStg] = id => {
    visualInfoProps on (Group.info.compose(group(id)))
  }
  
  def mapLookWithDefault[K,V](k : K, default : V) : Lens[Map[K,V],V] = Lens(m => m.getOrElse(k, default), (m, v) => m + ((k,v)))
  def mapUncheckedLook[K,V](k : K) : Lens[Map[K,V],V] = Lens(m => m(k), (m, v) => m + ((k,v)))
  
  def nodeVisualInfo : StgNode => Lens[VisualStg, VisualInfo] = node => {
    mapUncheckedLook(node) compose (VisualModel.nodesInfo compose VisualStg.visual)
  }
  
  sealed trait Shape
  object BezierShape extends Shape
  object PolylineShape extends Shape
  
  def visualArcShape : Lens[VisualArc, Shape] = Lens({
    case Polyline(_) => PolylineShape
    case Bezier(_,_) => BezierShape
  }, 
  (s, sh) => (s,sh) match {
    case (Polyline(_), PolylineShape) => s
    case (Bezier(_,_), BezierShape) => s
    case (Polyline(_), BezierShape) => Bezier(new RelativePoint(new Point2D.Double(0.3,0)), new RelativePoint(new Point2D.Double(0.7,0)))
    case (Bezier(_,_), PolylineShape) => Polyline(List())
  }
  )
  
  implicit val shapeChoice : Choice[Shape] = Choice(List(
    ("Polyline", PolylineShape)
    , ("Bezier", BezierShape)
    ))
  
  def visualArcProps : EditablePropertiesOf[VisualArc] = lensToEditableProperty(visualArcShape, "Shape")
  
  def place(placeId : Id[ExplicitPlace]) : Lens[VisualStg, ExplicitPlace] = Col.uncheckedLook(placeId).compose(MathStg.places).compose(VisualStg.math)
  
  def placeProps : EditablePropertiesOf[ExplicitPlace] = lensToEditableProperty(ExplicitPlace.initialMarking, "Initial marking")
  
  case class TransitionView
    ( signalName : String
    , label : Option[(SignalType, TransitionDirection)]
    )
  
  /*def transition(transId : Id[Transition]) : Lens[MathStg, TransitionView] = Lens(
    mathStg => {
      val trans = mathStg.transitions.unsafeLookup(transId)
      TransitionView(mathStg.signals.unsafeLookup(trans._1.), q,q),
    
    },
    y
  )*/
  type ExtendedSignalType = Option[SignalType]
  implicit val extendedSignalTypeChoice : Choice[ExtendedSignalType] = Choice (
    List(
      ("Dummy", None)
      , ("Input", Some(SignalType.Input))
      , ("Output", Some(SignalType.Output))
      , ("Internal", Some(SignalType.Internal))
    )
  )
  
  def getTransSignalName(transId : Id[Transition]) (stg : MathStg) = {
    stg.transitions(transId)._1 match {
      case DummyLabel(name) => name
      case SignalLabel(sig, _) => stg.signals.unsafeLookup(sig).name
    }
  }
  
  def setTransSignalName(transId : Id[Transition]) (stg : MathStg, newName : String) : MathStg = {
    stg.transitions(transId)._1 match {
      case DummyLabel(_) => MathStg.transitions.mod(stg, setTransitionLabel(transId)(DummyLabel(newName)))
      case SignalLabel(_, transDir) => {
        (for(sigId <- MathStg.signals.lifts(ensureSignal(newName, s => s.getOrElse(SignalType.Input)))
            ; _ <- MathStg.transitions.lifts(modify(setTransitionLabel(transId)(SignalLabel(sigId, transDir))))) yield sigId) ~> stg
      }
    }  
  }
 
  def ensureSignal(name : String, f : Option[SignalType] => SignalType) : State[Col[Signal], Id[Signal]] =
    state(signals => signals.map.find(kv => kv._2.name == name)
                                                   .map(kv => (Col.update(kv._1)(x => x.copy(direction = f(Some(x.direction)))) ~> signals,kv._1))
                                                   .getOrElse(Col.add(Signal(name, f(None)))(signals)))
  
  
  def ensureSignal(sig : Signal) : State[Col[Signal], Id[Signal]] = ensureSignal(sig.name, (t : Option[SignalType]) => sig.direction)
  
                                                  
  def setTransitionLabel(transId : Id[Transition])(label : TransitionLabel) : Col[Transition] => Col[Transition] =
    updateTransitionLabel(transId)(l => label)
  
  def updateTransitionLabel(transId : Id[Transition])(labelF : TransitionLabel => TransitionLabel) : Col[Transition] => Col[Transition] =
    transitions => {
      val label = labelF(transitions.lookup(transId).get._1)
      transitions.insert(transId)((label, StgOperations.allocInstanceNumber(transitions.remove(transId).map.values.toList, label)))
    }
  
  def transSignalName(transId : Id[Transition]) : Lens[MathStg, String] = {
      Lens(getTransSignalName(transId), setTransSignalName(transId))
    }
    
  def transSignalType(transId : Id[Transition]) : Lens[MathStg, ExtendedSignalType] = {
    def lens (mathStg : MathStg) : (ExtendedSignalType, ExtendedSignalType => MathStg) = {
    mathStg.transitions.unsafeLookup(transId)._1 match {
      case DummyLabel(name) => (None, {
        case None => mathStg
        case Some(sigType) => {
          (MathStg.signals.lifts(ensureSignal(Signal(name, sigType))) flatMap
            ((sig : Id[Signal]) => MathStg.transitions.lifts(modify(
                setTransitionLabel(transId)(SignalLabel(sig, TransitionDirection.Toggle))
                )))) ~> mathStg
        }
      })
      case SignalLabel(sigId, _) => {
        val sig = mathStg.signals.unsafeLookup(sigId)
        (Some(sig.direction),
          {
          	case None => MathStg.transitions.mod(mathStg, setTransitionLabel(transId)(DummyLabel(sig.name)))//todo:GC?
          	case Some(otherDirection) => MathStg.signals.lifts(Col.update(sigId)(s => Signal.direction.set(s, otherDirection)))~>mathStg
          }    
      )
      }
    }}
    Lens(stg => lens(stg)._1,(stg,x) => lens(stg)._2(x))
    }
  
    
  val transitionProps : Id[Transition] => EditablePropertiesOf[MathStg] = transId => {
    lensToEditableProperty(transSignalName(transId), "Signal name") ++
    lensToEditableProperty(transSignalType(transId), "Signal Type")
  }

  def emptyProps[T] : EditablePropertiesOf[T] = x => Expressions.constant(List())
  
  def objProperties : VisualEntity => EditablePropertiesOf[VisualStg] = {
    case NodeVisualEntity(node) => node match {
      case GroupVisualNode(grp) => groupProps(grp)
      case StgVisualNode(node) => visualInfoProps.on(nodeVisualInfo(node)) ++ (node match {
        case ExplicitPlaceNode(placeId) => placeProps.on(place(placeId))
        case TransitionNode(transId) => transitionProps(transId).on(VisualStg.math)
      })
    }
    case ArcVisualEntity(arc) => visualArcProps on mapLookWithDefault(arc, Polyline(List())) on (VisualModel.arcs compose VisualStg.visual) 
  }
}
