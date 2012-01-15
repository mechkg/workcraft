package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.util.Function;
import org.workcraft.util.Function2;
import org.workcraft.util.Graphics;

public class BoundedColorisableGraphicalContent {
	public static BoundedColorisableGraphicalContent EMPTY = new BoundedColorisableGraphicalContent(ColorisableGraphicalContent.EMPTY, new Rectangle2D.Double());
	
	
	public BoundedColorisableGraphicalContent(ColorisableGraphicalContent graphics, Rectangle2D.Double boundingBox) {
		this.graphics = graphics;
		this.boundingBox = boundingBox;
	}

	public final ColorisableGraphicalContent graphics;
	public final Rectangle2D.Double boundingBox;

	public static Function2<ColorisableGraphicalContent, Rectangle2D.Double, BoundedColorisableGraphicalContent> constructor = new Function2<ColorisableGraphicalContent, Rectangle2D.Double, BoundedColorisableGraphicalContent>() {
		@Override
		public BoundedColorisableGraphicalContent apply(ColorisableGraphicalContent argument1, Rectangle2D.Double argument2) {
			return new BoundedColorisableGraphicalContent(argument1, argument2);
		}
	};
	
	public static Function<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent> centerToZero = new Function<BoundedColorisableGraphicalContent, BoundedColorisableGraphicalContent>(){
		@Override
		public BoundedColorisableGraphicalContent apply(BoundedColorisableGraphicalContent argument) {
			return translate(argument, new Point2D.Double(-argument.boundingBox.getCenterX(), -argument.boundingBox.getCenterY()));
		}
	};
	
	public static BoundedColorisableGraphicalContent translate(final BoundedColorisableGraphicalContent argument, final Point2D offset) {
		Rectangle2D bb = argument.boundingBox;
		return new BoundedColorisableGraphicalContent(new ColorisableGraphicalContent(){
			@Override
			public void draw(DrawRequest request) {
				request.getGraphics().translate(offset.getX(), offset.getY());
				argument.graphics.draw(request);
			}
		}, new Rectangle2D.Double(
				  bb.getMinX() + offset.getX()
				, bb.getMinY() + offset.getY()
				, bb.getWidth()
				, bb.getHeight()));
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
