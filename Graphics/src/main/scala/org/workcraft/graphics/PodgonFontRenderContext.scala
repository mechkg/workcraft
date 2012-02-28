package org.workcraft.graphics
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

object PodgonFontRenderContext extends FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true)