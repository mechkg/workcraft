package org.workcraft.plugins.cpog.scala
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations
import org.workcraft.plugins.cpog.scala.nodes.Variable
import org.workcraft.scala.Util
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.expressions.Zero

object FormulaValue {
    def apply(formula: BooleanFormula[Variable]): Expression[BooleanFormula[Variable]] = new ExpressionBase[BooleanFormula[Variable]] {
      override def evaluate(context: EvaluationContext) =
        BooleanReplacer.cachedReplacer(BooleanOperations.worker, Util.asFunctionObject[Variable, BooleanFormula[Variable]]((v: Variable) => context.resolve(v.state) match {
          case VariableState.TRUE => One.instance[Variable]
          case VariableState.FALSE => Zero.instance[Variable]
          case _ => BooleanOperations.worker.`var`(v)
        }))(formula)
    }
}