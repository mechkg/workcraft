package org.workcraft.graphics.formularendering
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

object PodgonFontRenderContext {
	lazy val instance = new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true)
	def apply = instance
}