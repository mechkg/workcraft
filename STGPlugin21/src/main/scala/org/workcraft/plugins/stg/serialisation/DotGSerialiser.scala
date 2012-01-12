/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.stg.serialisation

import org.workcraft.dependencymanager.advanced.core.GlobalCache.eval
import java.io.OutputStream
import java.io.PrintWriter
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dom.Model
import org.workcraft.dom.Node
import org.workcraft.dom.NodeContext
import org.workcraft.dom.references.ReferenceManager
import org.workcraft.exceptions.FormatException
import org.workcraft.plugins.stg21.types._
import org.workcraft.serialisation.Format
import org.workcraft.serialisation.ReferenceProducer
import org.workcraft.util.Action1
import scala.util.Sorting

object DotGSerialiser {

	def writeSignalsHeader (out : PrintWriter, signalNames : Iterable[String], header : String) : Unit = {
		if (!signalNames.isEmpty) {
			
			val sortedNames = Sorting.stableSort(signalNames.toSeq)
			
			out.print(header)
			
			for (s <- sortedNames) {
				out.print(" ")
				out.print(s)
			}
			
			out.print("\n")
		}
	}
	
  def signalNames(stg : MathStg, typ : SignalType) = {
    stg.signals.map.values.filter(s => s.direction == typ).map(s => s.name)
  }

  def sortNodes (stg : MathStg, nodes : Seq[StgNode]) : Seq[StgNode] = {
    implicit val orderingExplicitPlace : Ordering[ExplicitPlace] = Ordering.Tuple2(Ordering.String, Ordering.Int).on {
      case (ExplicitPlace(marking, name)) => (name, marking)
    }
    
    def orderingFromCompare[T] : ((T, T) => Int) => Ordering[T] = f => new Ordering[T] {
      def compare(a : T, b : T) = f(a,b)
    }
    
    implicit val orderingSignalType = {
    	import SignalType._
    	Ordering.Int.on[SignalType]({
          case Input => 0
          case Output => 1
          case Internal => 2
        })
    }
    
    implicit val orderingSignal : Ordering[Signal] = Ordering.Tuple2(Ordering.String, orderingSignalType).on{
      case Signal(name, direction) => (name, direction)
    }
    
    implicit val orderingTransitionLabel= orderingFromCompare[TransitionLabel](
      {
        case (DummyLabel(_), SignalLabel(_, _)) => 1
        case (SignalLabel(_, _), DummyLabel(_)) => -1
        case (DummyLabel(a), DummyLabel(b)) => a compare b
        case (SignalLabel(sid1, dir1), SignalLabel(sid2, dir2)) => {
          Ordering.Option(orderingSignal).compare(stg.signals.lookup(sid1), stg.signals.lookup(sid2))
        }
      })
    implicit val orderingTransition = Ordering.Tuple2(orderingTransitionLabel, Ordering.Int)

    val orderingNode = orderingFromCompare[StgNode]({
      case (ExplicitPlaceNode(_), TransitionNode(_))  => 1
      case (TransitionNode(_), ExplicitPlaceNode(_))  => -1
      case (ExplicitPlaceNode(p1), ExplicitPlaceNode(p2))  => Ordering.Option (orderingExplicitPlace).compare (stg.places.lookup(p1), stg.places.lookup(p2))
      case (TransitionNode(t1), TransitionNode(t2))  => Ordering.Option (orderingTransition).compare (stg.transitions.lookup(t1), stg.transitions.lookup(t2))
    })
    
    nodes.sorted(orderingNode)
  }
  
  def toDotGData(stg : MathStg) : DotGData = {
    def showTransition : Transition => String = t => {
      val mainPart = t._1 match {
        case DummyLabel(name) => name
        case SignalLabel(sig, dir) => stg.signals.lookup(sig).get.name + (dir match {
          case TransitionDirection.Rise => "+"
          case TransitionDirection.Fall => "-"
          case TransitionDirection.Toggle => "~"
        })
      }
      val suffix = if(t._2==0) "" else "/"+t._2
      mainPart + suffix
    }
    def showNode: StgNode => String = {
      case ExplicitPlaceNode(pid) => stg.places.lookup(pid).map(_.name).get
      case TransitionNode(tid) => stg.transitions.lookup(tid).map(showTransition).get
    }
    DotGData(
        signalNames(stg, SignalType.Input),
        signalNames(stg, SignalType.Output),
        signalNames(stg, SignalType.Internal),
        stg.transitions.map.values.flatMap(t => t match {
		  case (DummyLabel(name),_) => Some(name)
		  case _ => None
		}),
		stg.places.map.values.map(_.name),
		stg.arcs.map.values.map(arc => (showNode(arc.first), showNode(arc.second))).groupBy(_._1).mapValues(_.map(_._2)),
		stg.places.map.values.filter(p => p.initialMarking != 0).map(p => (Left(p.name), p.initialMarking)) ++
		  stg.arcs.map.values.flatMap({
		    case ImplicitPlaceArc(from, to, marking) => Some (
		        (Right(showNode(TransitionNode(from)), showNode(TransitionNode(to))))
		        , marking
		        )
		    case _ => None
		  })
        )
  }
  
  def serialise(stg : MathStg, outStream : OutputStream) : Unit = {
    val out = new PrintWriter(outStream)
    writeMathStg(stg, out)
    out.close
  }
  
  def writeMathStg(stg : MathStg, out : PrintWriter) : Unit = {
    writeDotG(toDotGData(stg), out)
  }
  
  def writeDotG(dotG : DotGData, out : PrintWriter) : Unit = {
    out.print("# STG file generated by Workcraft -- http://workcraft.org\n")
    writeSignalsHeader(out, dotG.inputs, ".inputs")
    writeSignalsHeader(out, dotG.outputs, ".outputs")
    writeSignalsHeader(out, dotG.internals, ".internal")
    writeSignalsHeader(out, dotG.dummies, ".dummy")
    out.print(".graph\n")
    for(entry <- dotG.entries) {
      out.write(entry._1)
      for(item <- entry._2) {
        out.write(" ")
        out.write(item)
      }
      out.write("\n")
    }
    out.print(".marking {")
    val showPlaceRef : Either[String, (String, String)] => String = {
      case Left(s) => s
      case Right((t1, t2)) => "<"+t1+","+t2+">"
    }
    out.print(dotG.marking.map({
      case (place, count) => if(count==1) showPlaceRef(place) else showPlaceRef(place) + "=" + count.toString 
    }).mkString(" "))
    out.print("}\n")
    out.write(".end\n")
  }
  val getDescription = "Workcraft STG serialiser"
  val getExtension = ".g"
  val getFormatUUID = Format.STG
}
