package org.workcraft.plugins.dot.parser
 
import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._
 
/**
 * A parser for the GraphViz "dot" language. This code is in the public domain.
  
 *  @author Ross Judson
 */
class DotParser extends StdTokenParsers with ImplicitConversions {
  // Fill in abstract defs
  type Tokens = DotLexer
  val lexical = new Tokens
 
  // Configure lexical parsing
  lexical.reserved ++= List("strict", "graph", "digraph", "node", "edge", "subgraph")
  lexical.delimiters ++= List("{", "}", "[", "]", ":", "=", ";", ",", "->", "--","\"")
 
  import lexical._
   
  /** We want to map an Option of None to the empty string identifier,
  and Some(s) to s. 
  */
  implicit def emptyIdentifier(n: Option[String]) = n match {
    case Some(id) => id
    case _ => ""
  }
 
  /** It seems that when we need to get the implicit versions of sequences
  of results out into a list of those results, the automatic conversion
  can be performed by this implicit function. Note that this function 
  uses existential typing; we don't care about the type of the second part
  of the ~'s type, so we ignore it. */
  implicit def convertList[B](lst: List[~[B,_]]) = lst.map(_._1)
   
  //graph   :   [ strict ] (graph | digraph) [ ID ] '{' stmt_list '}'
  lazy val dot = ( (opt("strict") ^^ (_.isDefined)) ~ ("graph" ^^^ false | "digraph" ^^^ true) ~ (opt(ID)) ~ ("{" ~> stmt_list <~ "}")) ^^
    { case str ~ typ ~ id ~ statements => Graph(str, typ, id, statements:_*) }
     
  //stmt_list   :   [ stmt [ ';' ] [ stmt_list ] ]
  lazy val stmt_list = rep(stmt ~ opt(";")) 
   
  // stmt   :   node_stmt
  //    |   edge_stmt
  //    |   attr_stmt
  //    |   ID '=' ID
  //    |   subgraph
  lazy val stmt: Parser[Statement] = subgraph | attr_set | edge_stmt | attr_stmt | node_stmt
  lazy val attr_set = (ID <~ "=") ~ a_value ^^ 
    { case left ~ Pair(q,v) => Attr(left, Some(v), q) }
   
  //attr_stmt          :          (graph | node | edge) attr_list
  lazy val  attr_stmt = attr_list_type ~ attr_list ^^
    { case at ~ al => AttrList(at, al:_*) }
   
  lazy val attr_list_type =
    "graph" ^^^ "graph" |
    "node" ^^^ "node" |
    "edge" ^^^ "edge"
     
  //attr_list   :   '[' [ a_list ] ']' [ attr_list ]
  lazy val  attr_list = (("[" ~> a_list <~ "]")*) ^^ 
    { case lists => lists.flatMap(l => l) }
   
  //a_list      :   ID [ '=' ID ] [ ',' ] [ a_list ]
  lazy val  a_list = rep1sep (a_part, ",")
  lazy val  a_part =  
    (ID ~ opt("=" ~> a_value) ^^ { 
      case n ~ Some((q,v)) => Attr(n,Some(v),q)
      case n ~ None => Attr(n, None, false)
      } )
   
  lazy val a_value =
    accept("string", { case StringLit(v) => (true,v)}) |
    (ID ^^ { case v => (false,v) })
     
  lazy val a_string = log(accept("string", { case StringLit(v) => v }))("a_string")      
   
  //edge_stmt   :   (node_id | subgraph) edgeRHS [ attr_list ]
  lazy val  edge_stmt = ((node_id | subgraph) <~ "->") ~ rep1sep(node_id | subgraph, "->") ~ attr_list ^^
    { case head ~ rest ~ attrs => Edge("?", attrs, (head :: rest):_*) }
   
  //node_stmt   :   node_id [ attr_list ]
  lazy val  node_stmt = node_id ~ attr_list ^^ 
    { case Node(n,p) ~ a => Node(n,p,a:_*) } 
   
  //node_id     :   ID [ port ]
  lazy val  node_id = (ID ~ opt(port)) ^^
    { case n ~ p => Node(n, p) }
    
  //port    :   ':' ID [ ':' compass_pt ]
  //    |   ':' compass_pt
  lazy val  port = ":" ~> 
    ((ID ~ opt(":" ~> ID)) ^^ flatten2(Port) | 
     ID ^^ { Port (_, None) } ) 
   
  //subgraph    :   [ subgraph [ ID ] ] '{' stmt_list '}'
  lazy val  subgraph = ("subgraph" ~> opt(ID)) ~ ("{" ~> stmt_list <~ "}") ^^ 
    { case n ~ s => Subgraph(n, s:_*) }
     
  //compass_pt      :   (n | ne | e | se | s | sw | w | nw)
  lazy val compass_pt =
    "n" ^^^ "n"   | 
    "ne" ^^^ "ne" | 
    "e" ^^^ "e"   | 
    "se" ^^^ "se" | 
    "s" ^^^ "s"   | 
    "sw" ^^^ "sw" | 
    "w" ^^^ "w"   | 
    "nw" ^^^ "nw"
   
  lazy val ID = IDs | IDi | IDn
  lazy val IDn = accept("number", {case NumericLit(n) => n})
  lazy val IDs = accept("string", { case StringLit(n) => n })
  lazy val IDi = accept("identifier", { case Identifier(n) => n})
   
}