package org.workcraft.plugins.cpog.scala
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations
import org.workcraft.plugins.cpog.scala.nodes.Variable
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.plugins.cpog.scala.BooleanFormulaInstances._
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.expressions.Zero

object FormulaValue {
    def apply(formula: BooleanFormula[Variable]): Expression[BooleanFormula[Variable]] = {
      for(evaluated <- formula.map(v => for(s <- v.state) yield (v,s)).sequence)
        yield {
          evaluated.flatMap(x => {
            val (v,state)=x
            state match {
                case VariableState.TRUE => One.instance[Variable]
                case VariableState.FALSE => Zero.instance[Variable]
                case _ => BooleanOperations.worker.`var`(v)
            }
          })
      }
    }
}
