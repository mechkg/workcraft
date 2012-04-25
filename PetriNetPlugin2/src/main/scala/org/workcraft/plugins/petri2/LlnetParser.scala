package org.workcraft.plugins.petri2

import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader
import java.io.InputStream
import scala.collection.immutable.PagedSeq
import scala.util.parsing.input.PagedSeqReader
import java.awt.geom.Point2D
import java.io.File
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.StaticVisualConnectionData 
import java.io.BufferedReader
import java.io.FileReader
import scalaz._
import Scalaz._
import scala.util.matching.Regex
import org.workcraft.tasks.Task
import org.workcraft.tasks.TaskControl

object LlnetParser extends Parsers with RegexParsers {
  override type Elem = Char
  override val whiteSpace = """[ \t]+""".r

  private implicit def decorate[A](a: Option[A]) = new {
    def + (b: Option[A]): Option[A] = if (b.isDefined) b else a
  }

  trait HasNumName[A] {
    def auto (a: A, num: Int, name: String): A
  }

  implicit object BlockHasName extends HasNumName[BlockSpec] {
    def auto (a: BlockSpec, n: Int, nm: String) = {
     val x = if (a.num.isEmpty) a.copy(num = Some(n)) else a
     if (x.name.isEmpty) x.copy(name = Some(nm)) else x
    }
  }

  implicit object PlaceHasName extends HasNumName[PlaceSpec] {
    def auto (a: PlaceSpec, n: Int, nm: String) = {
     val x = if (a.num.isEmpty) a.copy(num = Some(n)) else a
     if (x.name.isEmpty) x.copy(name = Some(nm)) else x
    }
  }

  implicit object TransitionHasName extends HasNumName[TransitionSpec] {
    def auto (a: TransitionSpec, n: Int, nm: String) = {
     val x = if (a.num.isEmpty) a.copy(num = Some(n)) else a
     if (x.name.isEmpty) x.copy(name = Some(nm)) else x
    }
  }

  case class Llnet (format: Int, blocks: List[FinalBlockSpec], places: List[FinalPlaceSpec],
                    transitions: List[FinalTransitionSpec], producerArcs: List[FinalArcSpec],
                    consumerArcs: List[FinalArcSpec])

  case class FinalBlockSpec (num: Int, name: String, pos: (Int,Int), 
                        namePos: Option[(Int,Int)], meaning: Option[String], meaningPos: Option[(Int,Int)], 
                        reference: Option[String], blocks: Option[String])

  case class FinalTransitionSpec (num: Int, name: String, pos: (Int,Int), namePos: Option[(Int,Int)],
                        meaning: Option[String], meaningPos: Option[(Int,Int)], referece: Option[String],
                        blocks: Option[String], sync: Option[Boolean], phantom: Option[String])

  case class FinalPlaceSpec (num: Int, name: String, pos: (Int,Int), namePos: Option[(Int,Int)],
                        meaning: Option[String], meaningPos: Option[(Int,Int)], initialMarking: Int,
                        currentMarking: Int, capacity: Option[Int] , reference: Option[String],
                        entry: Option[Boolean], exit: Option[Boolean], blocks: Option[String])

  case class FinalArcSpec ( from: Int, to: Int, weight: Int, weightPos: Option[(Int,Int)] = None,
			   visibility: Option[Int] = None, anchorPoint: Option[(Int,Int)] = None )


  case class PlaceSpec (num: Option[Int] = None, name: Option[String] = None, pos: Option[(Int,Int)] = None, namePos: Option[(Int,Int)] = None,
                        meaning: Option[String] = None, meaningPos: Option[(Int,Int)] = None, initialMarking: Option[Int] = None,
                        currentMarking: Option[Int] = None, capacity: Option[Int] = None, reference: Option[String] = None,
                        entry: Option[Boolean] = None, exit: Option[Boolean] = None, blocks: Option[String] = None) {


                         def + (other: PlaceSpec) = 
                           PlaceSpec( num + other.num, name + other.name, pos + other.pos, namePos + other.namePos,
                                      meaning + other.meaning, meaningPos + other.meaningPos,
                                      initialMarking + other.initialMarking, currentMarking + other.currentMarking,
                                      capacity + other.capacity, reference + other.reference, entry + other.entry,
                                      exit + other.exit, blocks + other.blocks)

                         def finalise = FinalPlaceSpec(num.get, name.get, pos.get, namePos, meaning, meaningPos, initialMarking.getOrElse(0), currentMarking.getOrElse(0), capacity, 
                                                       reference, entry, exit, blocks)
                        }

  case class TransitionSpec (num: Option[Int] = None, name: Option[String] = None, pos: Option[(Int,Int)] = None, namePos: Option[(Int,Int)] = None,
                        meaning: Option[String] = None, meaningPos: Option[(Int,Int)] = None, reference: Option[String] = None,
                        blocks: Option[String] = None, sync: Option[Boolean] = None, phantom: Option[String] = None) {

                         def + (other: TransitionSpec) = 
                           TransitionSpec( num + other.num, name + other.name, pos + other.pos, namePos + other.namePos,
                                      meaning + other.meaning, meaningPos + other.meaningPos,
                                      reference + other.reference, blocks + other.blocks, sync + other.sync, phantom + other.phantom)

                         def finalise = FinalTransitionSpec(num.get, name.get, pos.get, namePos, meaning, meaningPos, reference, blocks, sync, phantom)
                        }


  case class ArcSpec ( from: Option[Int] = None, to: Option[Int] = None, weight: Option[Int] = None, weightPos: Option[(Int,Int)] = None,
                       visibility: Option[Int] = None, anchorPoint: Option[(Int,Int)] = None ) {

                         def + (other: ArcSpec) = 
                           ArcSpec( from + other.from,  to + other.to, weight + other.weight, 
				   weightPos + other.weightPos, visibility + other.visibility, 
				   anchorPoint + other.anchorPoint)

			 def finalise = FinalArcSpec (from.get, to.get, weight.getOrElse(1), weightPos, visibility, anchorPoint)
                        }

  case class BlockSpec ( num: Option[Int] = None, name: Option[String] = None, pos: Option[(Int,Int)] = None, 
                        namePos: Option[(Int,Int)] = None, meaning: Option[String] = None, meaningPos: Option[(Int,Int)] = None, 
                        reference: Option[String] = None, blocks: Option[String] = None) {

                         def + (other: BlockSpec) = 
                          BlockSpec( num + other.num, name + other.name, pos + other.pos, namePos + other.namePos,
                                      meaning + other.meaning, meaningPos + other.meaningPos,
                                      reference + other.reference, blocks + other.blocks)

                         def finalise = FinalBlockSpec (num.get, name.get, pos.get, namePos, meaning, meaningPos, reference, blocks)
                        }

  case class Text (pos: (Int, Int), text: String)

  val EOI: Parser[Any] = new Parser[Any] {
       def apply(in: Input) = {
           if (in.atEnd) new Success( "EOI", in )
           else Failure("end of file expected", in)
      }
  }

  def linebreak = """(%.*)?\r?\n""".r

  def endofline = (linebreak+) | EOI

  def integer = """\-?[0-9]+""".r ^^ (_.toInt)

  def position = (integer <~ "@") ~ integer ^^ { case x ~ y => (x,y) }

  def str = "\".*?\"".r ^^ { s => s.substring(1, s.length-1) }

  def header = ("PEP" ~ (endofline) ~ ("PetriBox"|"PTNet") ~ (endofline) ~ "FORMAT_N") ~> opt(integer) <~ (endofline) ^^ (_.getOrElse(1))

  def fixedSpecs = (opt(integer) ~ opt (str) ~ position)

  def placeSpec =  fixedSpecs flatMap { 
    case num_ ~ name_ ~ pos_ => placeOptSpec ^^ (PlaceSpec (num = num_, name = name_, pos = Some(pos_)) + _)
  }
    
  def placeOptSpec: Parser[PlaceSpec] =  
   ( ("n" ~> position) flatMap ( pos => placeOptSpec ^^ (PlaceSpec(namePos = Some(pos))+ _)) )      |
   ( ("b" ~> str) flatMap ( s => placeOptSpec ^^ (PlaceSpec(meaning = Some(s)) + _) ))              |
   ( ("a" ~> position) flatMap ( pos => placeOptSpec ^^ (PlaceSpec(meaningPos = Some(pos)) + _)) )  |
   ( ("M" ~> integer) flatMap ( im => placeOptSpec ^^ (PlaceSpec(initialMarking = Some(im)) + _)))  |
   ( ("m" ~> integer) flatMap ( m => placeOptSpec ^^ (PlaceSpec(currentMarking = Some(m)) + _)) )   |
   ( ("k" ~> integer) flatMap ( c =>  placeOptSpec ^^ (PlaceSpec(capacity = Some(c)) + _)) )        |
   ( ("R" ~> str) flatMap ( s =>  placeOptSpec ^^ (PlaceSpec(reference = Some(s)) + _)) )           |
   ( "e".r flatMap ( _ =>  placeOptSpec ^^ (PlaceSpec(entry = Some(true)) + _)) )                   |
   ( "x".r flatMap ( _ =>  placeOptSpec ^^ (PlaceSpec(exit = Some(true)) + _)) )                    |
   ( ("u" ~> str) flatMap ( s =>  placeOptSpec ^^ (PlaceSpec(blocks = Some(s)) + _)) )              |
   ( ("s" ~> integer) flatMap ( _ => placeOptSpec ) ) | // size(?), undocumented, ignored
   ( ("t" ~> integer) flatMap ( _ => placeOptSpec ) ) | // thickness(?), undocumented, ignored
   ( endofline ^^^ (PlaceSpec()) )

  def placeDefaults = "DPL" ~> placeOptSpec

  def places = ("PL" ~ endofline) ~> (placeSpec+)

  def transitionSpec = fixedSpecs flatMap { 
    case num_ ~ name_ ~ pos_ => transitionOptSpec ^^ (TransitionSpec (num = num_, name = name_, pos = Some(pos_)) + _) 
  }

  def transitionOptSpec: Parser[TransitionSpec] =
   ( ("n" ~> position) flatMap ( pos => transitionOptSpec ^^ (TransitionSpec(namePos = Some(pos)) + _)) )     |
   ( ("b" ~> str) flatMap ( s => transitionOptSpec ^^ (TransitionSpec(meaning = Some(s)) + _) ))              |
   ( ("a" ~> position) flatMap ( pos => transitionOptSpec ^^ (TransitionSpec(meaningPos = Some(pos)) + _)) )  |
   ( ("R" ~> str) flatMap ( s =>  transitionOptSpec ^^ (TransitionSpec(reference = Some(s)) + _)) )           |
   ( ("u" ~> str) flatMap ( s =>  transitionOptSpec ^^ (TransitionSpec(blocks = Some(s)) + _)) )              |
   (  "S".r flatMap ( _ =>  transitionOptSpec ^^ (TransitionSpec(sync = Some(true)) + _)) )                   |
   ( ("P" ~> str) flatMap ( s => transitionOptSpec ^^ (TransitionSpec(phantom = Some(s)) + _)) )              |
   ( ("s" ~> integer) flatMap ( _ => transitionOptSpec ) ) | // size(?), undocumented, ignored
   ( ("t" ~> integer) flatMap ( _ => transitionOptSpec ) ) | // thickness(?), undocumented, ignored
   ( ("v" ~> integer) flatMap ( _ => transitionOptSpec ) ) | // visibility(?), undocumented, ignored
   ( endofline ^^^ (TransitionSpec()) )

  def transitionDefaults = "DTR" ~> transitionOptSpec

  def transitions = ("TR" ~ endofline) ~> (transitionSpec+)

  def consumerArcSpec = ((integer <~ ">") ~ integer) flatMap { case from ~ to => arcOptSpec ^^ (ArcSpec (Some(from), Some(to)) + _) }

  // this makes no sense! why use '<' and '>' if they don't actually show the arc direction?
  def producerArcSpec = ((integer <~ "<") ~ integer) flatMap { case from ~ to => arcOptSpec ^^ (ArcSpec (Some(from), Some(to)) + _) }

  def arcOptSpec: Parser[ArcSpec] =
   ( ("w" ~> integer) flatMap ( w => arcOptSpec ^^ (ArcSpec(weight = Some(w)) + _)) )           |
   ( ("n" ~> position) flatMap ( wpos => arcOptSpec ^^ (ArcSpec(weightPos = Some(wpos)) + _) )) |
   ( ("v" ~> integer) flatMap ( v => arcOptSpec ^^ (ArcSpec(visibility = Some(v)) + _)) )       |
   ( ("J" ~> position) flatMap ( a =>  arcOptSpec ^^ (ArcSpec( anchorPoint = Some(a)) + _)) )   |
   ( ("t" ~> integer) flatMap ( _ => arcOptSpec ) ) | // thickness(?), undocumented, ignored
   ( endofline ^^^ (ArcSpec()) )

  def arcDefaults = "DPT" ~> arcOptSpec

  def consumerArcs = ("PT" ~ endofline) ~> (consumerArcSpec+)

  def producerArcs = ("TP" ~ endofline) ~> (producerArcSpec+)

  def blockSpec = fixedSpecs flatMap { 
    case num_ ~ name_ ~ pos_ => blockOptSpec ^^ (BlockSpec (num = num_, name = name_, pos = Some(pos_)) + _) 
  }

  def blockOptSpec: Parser[BlockSpec] = 
   ( ("n" ~> position) flatMap ( pos => blockOptSpec ^^ (BlockSpec(namePos = Some(pos)) + _)) )     |
   ( ("b" ~> str) flatMap ( s => blockOptSpec ^^ (BlockSpec(meaning = Some(s)) + _) ))              |
   ( ("a" ~> position) flatMap ( pos => blockOptSpec ^^ (BlockSpec(meaningPos = Some(pos)) + _)) )  |
   ( ("R" ~> str) flatMap ( s =>  blockOptSpec ^^ (BlockSpec(reference = Some(s)) + _)) )           |
   ( ("u" ~> str) flatMap ( s =>  blockOptSpec ^^ (BlockSpec(blocks = Some(s)) + _)) )              |
   ( endofline ^^^ (BlockSpec()) )

  def blockDefaults = "DBL" ~> blockOptSpec

  def blocks = ("BL" ~ endofline) ~> (blockSpec+)

  def text = (("TX" ~ endofline) ~> ("N" ~> position ~ str <~ endofline)+) ^^ (_.map { case pos ~ text => Text(pos, text) })

  def withAutoNum[A] (elements: List[A], nameBase: String)(implicit hasNum: HasNumName[A]) =
   elements.zipWithIndex.map { case (element, index) => hasNum.auto(element, (index+1), nameBase+(index+1)) }

  def formatN1 = opt(placeDefaults) ~ opt(transitionDefaults) ~
              opt(arcDefaults) ~ places ~ transitions ~ producerArcs ~ consumerArcs ~ opt(text) ^^ 
              { case placeDefaults ~ transitionDefaults ~ 
                     arcDefaults ~ places ~ transitions ~ producerArcs ~ consumerArcs ~ text => {
                        val placesWithDefaults =  if (placeDefaults.isDefined) places.map (placeDefaults.get + _) else places
                        val transitionsWithDefaults = if (transitionDefaults.isDefined) transitions.map (transitionDefaults.get + _) else transitions
                        val producerArcsWithDefaults = if (arcDefaults.isDefined) producerArcs.map(arcDefaults.get + _) else producerArcs
                        val consumerArcsWithDefaults = if (arcDefaults.isDefined) consumerArcs.map(arcDefaults.get + _) else consumerArcs

                        Llnet (1, List(), withAutoNum(placesWithDefaults, "p").map(_.finalise), 
                               withAutoNum(transitionsWithDefaults, "t").map(_.finalise), producerArcsWithDefaults.map(_.finalise), consumerArcsWithDefaults.map(_.finalise))
                     }
              }

  def formatN2 = failure ("FORMAT_N2 is not supported")

  def llnet = header flatMap {
    case 1 => formatN1
    case 2 => formatN2
    case 3 => failure ("Expected FORMAT_N or FORMAT_N2")
  }

  def parseLlnet(file: File) = 
    ioPure.pure {
      parse(phrase(llnet), (new BufferedReader(new FileReader(file)))) match {
	case Success(r, _) => Right(r)
	case err => Left(err.toString)
      }
    }
				    
}


object PetriNetBuilder {
  import LlnetParser._
  import PetriNet._
  def buildPetriNet (llnet: Llnet): IO[VisualPetriNet] = {
    for {
      places <- llnet.places.traverse(p => newPlace.map((p.num, p.name, p.initialMarking, p.pos,  _)));
      val placeMap = places.map ( p => (p._1, p._5)).toMap;
      transitions <- llnet.transitions.traverse (t => newTransition.map ((t.num, t.name, t.pos,  _ )));
      val transitionMap = transitions.map( t => (t._1, t._4)).toMap;
      parcs <- llnet.producerArcs.traverse ( a => newProducerArc ( transitionMap(a.from), placeMap(a.to) ) )
      carcs <- llnet.consumerArcs.traverse ( a => newConsumerArc ( placeMap(a.from), transitionMap(a.to) ) )
    } yield {

      val scale = 20

      val netWithPlaces = places.foldLeft(VisualPetriNet.Empty) {
	case (VisualPetriNet(net, layout, varcs), (num, name, marking, pos, place)) => 
	  VisualPetriNet (net.copy (places = place :: net.places, labelling = net.labelling + (place -> name), 
				    marking = net.marking + (place -> marking)), 
			  layout + (place -> new Point2D.Double (pos._1.toDouble / scale, pos._2.toDouble / scale)),
			  varcs)
      }

      val netWithTransitions = transitions.foldLeft(netWithPlaces) { 
	case (VisualPetriNet(net, layout, varcs), (num, name, pos, t)) => 
	  VisualPetriNet(net.copy (transitions = t :: net.transitions, labelling = net.labelling + (t -> name)),
			 layout + (t -> new Point2D.Double (pos._1.toDouble / scale, pos._2.toDouble / scale)),
			 varcs)
      }

      (parcs ++ carcs).foldLeft(netWithTransitions) {
	case (VisualPetriNet(net, layout, varcs), arc) => 
	  VisualPetriNet(net.copy (arcs = arc :: net.arcs), 
			 layout, 
			 varcs + (arc -> new Polyline(List())))
      }
    }
  }
}
