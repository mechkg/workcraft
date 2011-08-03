package org.workcraft.plugins.stg21

import types._

object StgOperations {
  
  import scalaz.State
  import scalaz.State._
  
  val createMathPlace : State[MathStg, Id[Place]] =
    MathStg.withPlaces(Col.add(Place(0)))
  
  
  def createPlace(where : Point2D) : State[VisualStg, Id[Place]] ={
    import VisualStg._
    for(p <- onMathStg(createMathPlace);
      _ <- onVisualModel(VisualModel.addNode(PlaceNode(p), where)))
      yield p;
  }
  
  def removePlaceS(place : Id[Place]) : State[VisualStg, Unit] = {
    import VisualStg._
    for(_ <- onMathStg(MathStg.withPlaces(Col.remove(place))));
      _ <- onVisualModel()
  }
  
  def removePlace(place : Id[Place]) (visualStg : VisualStg) : VisualStg = 
    VisualStg(
        visualStg.stg.copy(places = visualStg.stg.places - place)),
        visualStg.visualStg.copy(nodesInfo = visualStg.visualStg.nodesInfo - PlaceNode(place))
  
  def createPlace(where : Point2D) (visualStg : VisualStg) : (VisualStg, Id[Place]) = visualStg match {
    case VisualStg (Stg(signals, transitions, places, arcs), visual) => {
      
      withPlaces(_.add(Place(0)))
      val (newPlaces, p) = places.add(Place(0))
      (VisualStg(Stg(signals, transitions, newPlaces, arcs), visual + ), p)
    }
  }
  
  def removePlace(place : Id[Place])(visualStg : VisualStg) : VisualStg = visualStg match {
    case 
  }
    
    val p = Place[ModifiableExpression](sm.create(0))
    val pr = sm.create(p)
    visualStg.stg.places.modify(places => p :: places)
    visualStg.visual += ()
  }
  
}
