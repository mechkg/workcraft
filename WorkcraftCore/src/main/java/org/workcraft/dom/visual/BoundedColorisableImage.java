package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Graphics;

public class BoundedColorisableImage {
	public BoundedColorisableImage(ColorisableGraphicalContent graphics, Rectangle2D boundingBox) {
		this.graphics = graphics;
		this.boundingBox = boundingBox;
	}

	public final ColorisableGraphicalContent graphics;
	public final Rectangle2D boundingBox;

	public static Function2<ColorisableGraphicalContent, Rectangle2D, BoundedColorisableImage> constructor = new Function2<ColorisableGraphicalContent, Rectangle2D, BoundedColorisableImage>() {
		@Override
		public BoundedColorisableImage apply(ColorisableGraphicalContent argument1, Rectangle2D argument2) {
			return new BoundedColorisableImage(argument1, argument2);
		}
	};
	
	public static Function<BoundedColorisableImage, BoundedColorisableImage> centerToZero = new Function<BoundedColorisableImage, BoundedColorisableImage>(){
		@Override
		public BoundedColorisableImage apply(BoundedColorisableImage argument) {
			return translate(argument, new Point2D.Double(-argument.boundingBox.getCenterX(), -argument.boundingBox.getCenterY()));
		}
	};
	
	public static BoundedColorisableImage translate(final BoundedColorisableImage argument, final Point2D offset) {
		Rectangle2D bb = argument.boundingBox;
		return new BoundedColorisableImage(new ColorisableGraphicalContent(){
			@Override
			public void draw(DrawRequest request) {
				request.getGraphics().translate(offset.getX(), offset.getY());
				argument.graphics.draw(request);
			}
		}, new Rectangle2D.Double(
				  bb.getMinX() + offset.getX()
				, bb.getMaxX() + offset.getX()
				, bb.getWidth()
				, bb.getHeight()));
	}
	
	public static Function<BoundedColorisableImage, Rectangle2D> getBoundingBox = new Function<BoundedColorisableImage, Rectangle2D>(){
		@Override
		public Rectangle2D apply(BoundedColorisableImage image) {
			return image.boundingBox;
		}
	};
	
	public static Function<BoundedColorisableImage, ColorisableGraphicalContent> getGraphics = new Function<BoundedColorisableImage, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(BoundedColorisableImage image) {
			return image.graphics;
		}
	};
	
	public static Function2<BoundedColorisableImage, BoundedColorisableImage , BoundedColorisableImage> compose = new Function2<BoundedColorisableImage, BoundedColorisableImage, BoundedColorisableImage>() {
		@Override
		public BoundedColorisableImage apply(BoundedColorisableImage argument1, BoundedColorisableImage argument2) {
			return compose(argument1, argument2);
		}
	};
	
	public static BoundedColorisableImage compose(BoundedColorisableImage image1, BoundedColorisableImage image2) {
		return new BoundedColorisableImage(Graphics.compose(image1.graphics, image2.graphics),
				BoundingBoxHelper.union(image1.boundingBox, image2.boundingBox));
	}
}
