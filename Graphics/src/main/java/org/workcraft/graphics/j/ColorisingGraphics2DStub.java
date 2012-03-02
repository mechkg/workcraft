package org.workcraft.graphics.j;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public abstract class ColorisingGraphics2DStub extends Graphics2D {
	Graphics2D g2d;
	
	public ColorisingGraphics2DStub (final Graphics2D delegate) {
		g2d = delegate;
	}

	public void addRenderingHints(Map<?, ?> hints) {
		g2d.addRenderingHints(hints);
	}

	public void clearRect(int x, int y, int width, int height) {
		g2d.clearRect(x, y, width, height);
	}

	public void clip(Shape s) {
		g2d.clip(s);
	}

	public void clipRect(int x, int y, int width, int height) {
		g2d.clipRect(x, y, width, height);
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		g2d.copyArea(x, y, width, height, dx, dy);
	}

	public Graphics create() {
		return g2d.create();
	}

	public Graphics create(int x, int y, int width, int height) {
		return g2d.create(x, y, width, height);
	}

	public void dispose() {
		g2d.dispose();
	}

	public void draw(Shape s) {
		g2d.draw(s);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		g2d.draw3DRect(x, y, width, height, raised);
	}

	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g2d.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		g2d.drawBytes(data, offset, length, x, y);
	}

	public void drawChars(char[] data, int offset, int length, int x, int y) {
		g2d.drawChars(data, offset, length, x, y);
	}

	public void drawGlyphVector(GlyphVector g, float x, float y) {
		g2d.drawGlyphVector(g, x, y);
	}

	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		g2d.drawImage(img, op, x, y);
	}

	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return g2d.drawImage(img, xform, obs);
	}

	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return g2d.drawImage(img, x, y, bgcolor, observer);
	}

	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return g2d.drawImage(img, x, y, observer);
	}

	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return g2d.drawImage(img, x, y, width, height, observer);
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				bgcolor, observer);
	}

	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				observer);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		g2d.drawLine(x1, y1, x2, y2);
	}

	public void drawOval(int x, int y, int width, int height) {
		g2d.drawOval(x, y, width, height);
	}

	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawPolygon(Polygon p) {
		g2d.drawPolygon(p);
	}

	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.drawPolyline(xPoints, yPoints, nPoints);
	}

	public void drawRect(int x, int y, int width, int height) {
		g2d.drawRect(x, y, width, height);
	}

	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		g2d.drawRenderableImage(img, xform);
	}

	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		g2d.drawRenderedImage(img, xform);
	}

	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		g2d.drawString(iterator, x, y);
	}

	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		g2d.drawString(iterator, x, y);
	}

	public void drawString(String str, float x, float y) {
		g2d.drawString(str, x, y);
	}

	public void drawString(String str, int x, int y) {
		g2d.drawString(str, x, y);
	}

	public boolean equals(Object obj) {
		return g2d.equals(obj);
	}

	public void fill(Shape s) {
		g2d.fill(s);
	}

	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		g2d.fill3DRect(x, y, width, height, raised);
	}

	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g2d.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void fillOval(int x, int y, int width, int height) {
		g2d.fillOval(x, y, width, height);
	}

	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		g2d.fillPolygon(xPoints, yPoints, nPoints);
	}

	public void fillPolygon(Polygon p) {
		g2d.fillPolygon(p);
	}

	public void fillRect(int x, int y, int width, int height) {
		g2d.fillRect(x, y, width, height);
	}

	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		g2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void finalize() {
		g2d.finalize();
	}

	public Color getBackground() {
		return g2d.getBackground();
	}

	public Shape getClip() {
		return g2d.getClip();
	}

	public Rectangle getClipBounds() {
		return g2d.getClipBounds();
	}

	public Rectangle getClipBounds(Rectangle r) {
		return g2d.getClipBounds(r);
	}

	public Rectangle getClipRect() {
		return g2d.getClipRect();
	}

	public Color getColor() {
		return g2d.getColor();
	}

	public Composite getComposite() {
		return g2d.getComposite();
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return g2d.getDeviceConfiguration();
	}

	public Font getFont() {
		return g2d.getFont();
	}

	public FontMetrics getFontMetrics() {
		return g2d.getFontMetrics();
	}

	public FontMetrics getFontMetrics(Font f) {
		return g2d.getFontMetrics(f);
	}

	public FontRenderContext getFontRenderContext() {
		return g2d.getFontRenderContext();
	}

	public Paint getPaint() {
		return g2d.getPaint();
	}

	public Object getRenderingHint(Key hintKey) {
		return g2d.getRenderingHint(hintKey);
	}

	public RenderingHints getRenderingHints() {
		return g2d.getRenderingHints();
	}

	public Stroke getStroke() {
		return g2d.getStroke();
	}

	public AffineTransform getTransform() {
		return g2d.getTransform();
	}

	public int hashCode() {
		return g2d.hashCode();
	}

	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return g2d.hit(rect, s, onStroke);
	}

	public boolean hitClip(int x, int y, int width, int height) {
		return g2d.hitClip(x, y, width, height);
	}

	public void rotate(double theta, double x, double y) {
		g2d.rotate(theta, x, y);
	}

	public void rotate(double theta) {
		g2d.rotate(theta);
	}

	public void scale(double sx, double sy) {
		g2d.scale(sx, sy);
	}

	abstract public void setBackground(Color color);

	public void setClip(int x, int y, int width, int height) {
		g2d.setClip(x, y, width, height);
	}

	public void setClip(Shape clip) {
		g2d.setClip(clip);
	}

	abstract public void setColor(Color c);
		
	public void setComposite(Composite comp) {
		g2d.setComposite(comp);
	}

	public void setFont(Font font) {
		g2d.setFont(font);
	}

	public void setPaint(Paint paint) {
		g2d.setPaint(paint);
	}

	public void setPaintMode() {
		g2d.setPaintMode();
	}

	public void setRenderingHint(Key hintKey, Object hintValue) {
		g2d.setRenderingHint(hintKey, hintValue);
	}

	public void setRenderingHints(Map<?, ?> hints) {
		g2d.setRenderingHints(hints);
	}

	public void setStroke(Stroke s) {
		g2d.setStroke(s);
	}

	public void setTransform(AffineTransform Tx) {
		g2d.setTransform(Tx);
	}

	public void setXORMode(Color c1) {
		g2d.setXORMode(c1);
	}

	public void shear(double shx, double shy) {
		g2d.shear(shx, shy);
	}

	public String toString() {
		return g2d.toString();
	}

	public void transform(AffineTransform Tx) {
		g2d.transform(Tx);
	}

	public void translate(double tx, double ty) {
		g2d.translate(tx, ty);
	}

	public void translate(int x, int y) {
		g2d.translate(x, y);
	}
}