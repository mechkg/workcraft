package org.workcraft.graphics

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

case class BoundedColorisableGraphicalContent (cgc: ColorisableGraphicalContent, boundingBox: Rectangle2D) {
  def centerToZero = translate(new Point2D.Double(-argument.boundingBox.getCenterX(), -argument.boundingBox.getCenterY())
  def translate (offset: Point2D) = BoundedColorisableGraphicalContent(ColorisableGraphicalContent {
			def draw(request: DrawRequest) = {
				request.getGraphics().translate(offset.getX(), offset.getY())
				argument.graphics.draw(request);
			}
		}, new Rectangle2D.Double(
				  bb.getMinX() + offset.getX()
				, bb.getMinY() + offset.getY()
				, bb.getWidth()
				, bb.getHeight()));
    
  }
	
	public static Function<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent> centerToZero = new Function<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent>(){
		@Override
		public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent argument) {
			return translate(argument, new Point2D.Double(-argument.boundingBox.getCenterX(), -argument.boundingBox.getCenterY()));
		}
	};
	
	public static BoundedColorisableGraphicalContent translate(final BoundedColorisableGraphicalContent argument, final Point2D offset) {
	}
	
	public static Function2<BoundedColorisableGraphicalContent, Point2D, BoundedColorisableGraphicalContent> translateQweqwe = new Function2<BoundedColorisableGraphicalContent, Point2D, BoundedColorisableGraphicalContent>() { 
		@Override
		public BoundedColorisableGraphicalContent apply(
				BoundedColorisableGraphicalContent argument1, Point2D argument2) {
			return translate(argument1, argument2);
		}
	};
	
	public static Function<BoundedColorisableGraphicalContent, Rectangle2D> getBoundingBox = new Function<BoundedColorisableGraphicalContent, Rectangle2D>(){
		@Override
		public Rectangle2D apply(BoundedColorisableGraphicalContent image) {
			return image.boundingBox;
		}
	};
	
	public static Function<BoundedColorisableGraphicalContent, ColorisableGraphicalContent> getGraphics = new Function<BoundedColorisableGraphicalContent, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(BoundedColorisableGraphicalContent image) {
			return image.graphics;
		}
	};
	
	public static Function2<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent , BoundedColorisableGraphicalContent> composeFunc = new Function2<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent>() {
		@Override
		public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent argument1, BoundedColorisableGraphicalContent argument2) {
			return compose(argument1, argument2);
		}
	};
	
	public static BoundedColorisableGraphicalContent compose(BoundedColorisableGraphicalContent image1, BoundedColorisableGraphicalContent image2) {
		return new BoundedColorisableGraphicalContent(Graphics.compose(image1.graphics, image2.graphics),
				BoundingBoxHelper.union(image1.boundingBox, image2.boundingBox));
	}
}

object BoundedColorisableGraphicalContent {
  val Empty = BoundedColorisableGraphicalContent (ColorisableGraphicalContent.Empty, new Rectangle2D.Double())
}