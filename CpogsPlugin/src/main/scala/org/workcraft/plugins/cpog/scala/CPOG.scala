package org.workcraft.plugins.cpog.scala
import org.workcraft.util.Function0
import org.workcraft.plugins.cpog.CheckedPrefixNameGen
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.scala.StorageManager
import org.workcraft.plugins.cpog.scala.nodes._

object CPOG {
  def apply(storage : StorageManager) : CPOG = apply(storage, Nil, Nil, Nil, Nil)
  def apply(storage : StorageManager, 
      variables : List[Variable], 
      vertices : List[Vertex], 
      rhoClauses : List[RhoClause],
      arcs : List[Arc]) = new CPOG(storage, 
          storage.create(variables), 
          storage.create(vertices),
          storage.create(rhoClauses),
          storage.create(arcs))
}

class CPOG(val storage : StorageManager,
    val variables : ModifiableExpression[List[org.workcraft.plugins.cpog.scala.nodes.Variable]],
    val vertices : ModifiableExpression[List[org.workcraft.plugins.cpog.scala.nodes.Vertex]],
    val rhoClauses : ModifiableExpression[List[org.workcraft.plugins.cpog.scala.nodes.RhoClause]],
    val arcs : ModifiableExpression[List[org.workcraft.plugins.cpog.scala.nodes.Arc]]
  ) {

  val varNameGen : Function0[String] = new CheckedPrefixNameGen("x_", (candidate : java.lang.String) => {
			eval(variables).foldLeft(true)((b, v) => b && !eval(v.visualProperties.label).equals(candidate))
		} : java.lang.Boolean
	)
	
  val vertNameGen : Function0[String] = new CheckedPrefixNameGen("v_", (candidate : java.lang.String) => {
    val v : List[Vertex] = eval(vertices)
    def ff(b : java.lang.Boolean,v : Vertex) : java.lang.Boolean = b && !eval(v.visualProperties.label).equals(candidate)
    v.foldLeft(true : java.lang.Boolean)(ff)
  })

  def add[T](vec : ModifiableExpression[List[T]], item : T) {
    vec.modify(item :: _)
  }

  def connect(first : Vertex, second : Vertex) : Arc = {
    val con : Arc = Arc.create(storage, first, second)
    add(arcs, con)
    con
  }

  def createVertex : Vertex = {
    val vertex = Vertex.create(storage)
    vertex.visualProperties.label.setValue(vertNameGen.apply)
    add(vertices, vertex);
    return vertex;
  }

  def createRhoClause : RhoClause = {
    val rhoClause = RhoClause.create(storage)
    add(rhoClauses, rhoClause)
    return rhoClause
  }

  def createVariable : Variable = {
    val name = varNameGen.apply
    val v = Variable.create(storage, storage.create(name))
    add(variables, v);
    return v;
  }

  val components : Expression[List[Component]] = {
    for(variables <- variables;
      vertices <- vertices;
      rhoClauses <- rhoClauses)
    yield variables ::: vertices ::: rhoClauses
  }

  val nodes : Expression[List[Node]] = {
    for(c <- components;
    a <- arcs)
      yield c ::: a
  }
}