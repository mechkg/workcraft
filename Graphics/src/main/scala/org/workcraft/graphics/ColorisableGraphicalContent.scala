package org.workcraft.graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

trait ColorisableGraphicalContent {
  def draw(request: DrawRequest)
  
  def applyColorisation(color: Colorisation) = new GraphicalContent {
    def draw (graphics: Graphics2D) = ColorisableGraphicalContent.this.draw (DrawRequest(graphics, color))
  }

  def transform (transformation: AffineTransform) = 
    ColorisableGraphicalContent (applyColorisation(_).transform(transformation))
  
  def compose (top: ColorisableGraphicalContent) =
    ColorisableGraphicalContent (c => applyColorisation(c).compose(top.applyColorisation(c)))
}

object ColorisableGraphicalContent {
	val Empty = new ColorisableGraphicalContent {
		def draw(request: DrawRequest) = {}
	}
	
	def apply (f: Colorisation => GraphicalContent) = new ColorisableGraphicalContent {
	  def draw (request: DrawRequest) = f(request.colorisation).draw(request.graphics)
	}
}