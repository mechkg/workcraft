package org.workcraft.dom.visual.connections
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.TouchableC
import org.workcraft.graphics.ParametricCurve

case class ConnectionGui(
    shape : TouchableC
    , graphicalContent : ColorisableGraphicalContent
    , parametricCurve : ParametricCurve)
