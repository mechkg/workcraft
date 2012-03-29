package org.workcraft.plugins.dot.parser
import java.io.File
import java.io.FileReader
import scala.collection.immutable.PagedSeq
import scala.util.parsing.input.PagedSeqReader
import org.workcraft.tasks.Task
import org.workcraft.tasks.TaskControl
import scalaz.Scalaz._
import org.workcraft.scala.effects.IO._

object Dot extends DotParser {

  def parse(input: String) =
    phrase(dot)(new lexical.Scanner(input)) match {
      case Success(result, _) => println("Success!"); Some(result)
      case n @ _ => println(n); None
    }

  // Ugly hack for dot's idiotic backslash line wraps -- too sleepy to figure out how to do that properly
  def parse(input: File) = phrase(dot)(new lexical.Scanner(scala.io.Source.fromFile(input).getLines.toList.mkString("").replace("\\\n",""))) match {
    case Success(result, _) => Right(result)
    case err => Left(err.toString)
  }
  /*def parse(input: File) = phrase(dot)(new lexical.Scanner(new PagedSeqReader(PagedSeq.fromFile(input)))) match {
    case Success(result, _) => Right(result)
    case err => Left(err.toString) 
  }*/
  
  def parseTask (file: File) = new Task[Graph, String] {
    def runTask(tc: TaskControl) = tc.descriptionUpdate ("Reading " + file.getPath) >>=| ( ioPure.pure { parse(file) } map {case Right(result) => Right(result); case Left(error) => Left(Some(error))})
  }  

  def main(args: Array[String]) {
    val x = parse("""
      digraph acm {
        hello -> world;
        test:up:n -> world;
        style = filled;
         
        subgraph cluster {
          node [style=filled,color=white];
          toast -> bingo;
          zot -> bingo;
          zot -> test;
          style=filled;
          color=lightgrey;
          label = "Below";
        }        
 
    }
     
    """)

    println(x)

  }
}

abstract class DotComponent {
  override def toString = {
    val b = new StringBuilder
    buildString(0, b)
    b.toString()
  }

  private def indent(level: Int, b: StringBuilder) {
    for (i <- 0 to level) b append ' '
  }

  def buildString(implicit level: Int, b: StringBuilder) {

    def between(sep: String, things: Seq[DotComponent])(implicit lev: Int) {
      var first = true
      for (t <- things) {
        if (first) first = false else b append sep
        t.buildString(lev, b)
      }
    }

    def betweenList(before: String, sep: String, after: String, things: Seq[DotComponent])(implicit lev: Int) {
      if (!things.isEmpty) {
        b append before
        between(sep, things)(lev)
        b append after
      }
    }

    this match {
      case Port(id, compass) =>
        b append id
        if (compass != None) b append ':' append compass.get
      case Graph(strict, digraph, id, statements @ _*) =>
        indent(level, b)
        if (strict) b append "strict "
        b append (if (digraph) "digraph " else "graph ")
        betweenList(id + " {\n", "\n", "}\n", statements)(level + 1)
      case AttrList(kind, attrs @ _*) =>
        indent(level, b)
        b append kind
        betweenList(" [", ",", "]", attrs)(0)
      case Attr(n, Some(v), q) =>
        indent(level, b)
        b append n append '='
        if (q) b append '"'
        b append v
        if (q) b append '"'
      case Attr(n, _, _) => b append n
      case Edge(_, attrs, nodes @ _*) =>
        indent(level, b)
        between(" -> ", nodes)
        betweenList(" [", ",", "]", attrs)(0)
      case Subgraph(id, statements @ _*) =>
        indent(level, b)
        b append "subgraph " append id
        betweenList(" {\n", "\n", "\n", statements)(level + 1)
        indent(level, b)
        b append "}\n"
      case Node(id, port, attrs @ _*) =>
        b append id
        if (port != None) { b append ':'; port.get.buildString(level, b) }
        betweenList(" [", ", ", " ]\n", attrs)
    }
  }

}

/**
 * Implemented by DOT components that are allowed to have an identity
 */
trait Identified extends DotComponent {
  val id: String
}

/**
 * Implemented by DOT components that are statements, for use within
 * graphs and subgraphs.
 */
trait Statement extends DotComponent

/**
 * Implemented by DOT components that have a list of attributes associated
 * with them.
 */
trait Attributed extends DotComponent {
  val attrs: Seq[Attr]
}

/**
 * Implemented by DOT components that can participate in an edge; currently
 * Node and Subgraph.
 */
trait EdgeComponent extends Identified

/**
 * The abstract base for the two graph components of DOT -- graph (digraph)
 * and subgraph.
 */
abstract class AbstractGraph extends DotComponent with Identified {
  val statements: Seq[Statement]
}
/**
 * Nodes can have an optional port identifier. The port identifier can have
 * an optional compass direction.
 */
case class Port(id: String, compass: Option[String]) extends DotComponent

case class Graph(strict: Boolean, digraph: Boolean, id: String, statements: Statement*) extends AbstractGraph

//
// Statements
//
case class AttrList(kind: String, attrs: Attr*) extends Statement with Attributed
case class Attr(name: String, value: Option[String], quoted: Boolean) extends Statement
case class Edge(id: String, attrs: Seq[Attr], nodes: EdgeComponent*) extends Identified with Statement with Attributed
case class Subgraph(id: String, statements: Statement*) extends AbstractGraph with EdgeComponent with Statement
case class Node(id: String, port: Option[Port], attrs: Attr*) extends EdgeComponent with Statement with Attributed