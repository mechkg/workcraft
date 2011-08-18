
import org.junit.Assert._

import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.VisualArc
import org.workcraft.plugins.cpog.scala.nodes.snapshot.SnapshotMaker
import org.workcraft.graphics.LabelPositioning
import java.awt.geom.Point2D
import scala.collection.JavaConversions._
import org.workcraft.dom.visual.connections.RelativePoint
import org.workcraft.scala.Expressions.eval

class SnapshotsTest {
    val sm = new org.workcraft.scala.StorageManager(new org.workcraft.plugins.stg.HistoryPreservingStorageManager)

    def mkSnapshot = {
        val one = org.workcraft.plugins.cpog.optimisation.expressions.One.instance[Variable]
        val vert1 = ( Vertex
            ( sm.create(one)
            , VisualProperties
                ( sm.create("vert1")
                , sm.create(LabelPositioning.BOTTOM)
                , sm.create(new Point2D.Double(0, 0))
                )
            )
        )
        val vert2 = ( Vertex
            ( sm.create(one)
            , VisualProperties
                ( sm.create("vert2")
                , sm.create(LabelPositioning.BOTTOM)
                , sm.create(new Point2D.Double(1, 0))
                )
            )
        )
        val arc = ( Arc
            ( vert1
            , vert2
            , sm.create(one)
            , sm.create
                (VisualArc.Bezier
                    ( sm.create(RelativePoint.ONE_THIRD)
                    , sm.create(RelativePoint.TWO_THIRDS)
                    )
                )
            )
        )
        val nodes = vert1 :: vert2 :: arc :: Nil : List[Node]
        eval(SnapshotMaker.doMakeSnapshot(nodes))
    }

    @org.junit.Test
    def test1 {
        val snapshot = mkSnapshot
        assertEquals(2, snapshot.vertices.size)
    }
    
    @org.junit.Test
    def test2 {
        val snapshot = mkSnapshot
        val cpog = org.workcraft.plugins.cpog.scala.serialisation.SnapshotLoader.load(snapshot, sm)
        val vertices = (for(v @ Vertex(_,_) <- eval(cpog.components)) yield v).toSet
        val (arc : Arc) :: Nil = (for(a @ Arc (_, _, _, _) <- eval(cpog.nodes)) yield a).toList
        val arcEnds = (arc.first :: arc.second :: Nil).toSet
        assertEquals(vertices, arcEnds)
    }
}

