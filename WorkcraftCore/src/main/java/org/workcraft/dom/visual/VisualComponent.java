/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 * 
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements DependentNode {
	private final MathNode refNode;

	private final static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1)
			.deriveFont(0.5f);

	public static FontRenderContext podgonFontRenderContext() {
		return new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true);
	}
	
	private final ModifiableExpression<String> labelText;
	private final Label label;
	
	private final ModifiableExpression<Color> labelColor;
	private final ModifiableExpression<Color> foregroundColor;
	private final ModifiableExpression<Color> fillColor;

	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label", label(), String.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label color", labelColor(), Color.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Foreground color", foregroundColor(), Color.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Fill color", fillColor(), Color.class));
	}

	public VisualComponent(MathNode refNode, StorageManager storage) {
		super(storage);
		this.refNode = refNode;

		labelText = storage.create("");
		label = new Label(labelFont, labelText);
		
		labelColor = storage.create(CommonVisualSettings.getForegroundColor());
		foregroundColor = storage.create(CommonVisualSettings.getForegroundColor());
		fillColor = storage.create(CommonVisualSettings.getFillColor());
		
		addPropertyDeclarations();
	}
	public ModifiableExpression<String> label() {
		return labelText;
	}

	Point2D labelPosition(EvaluationContext context) {
		if(true)
			throw new NotImplementedException("The label rendering requires a bounding box, which is not necessarily available here");
		Rectangle2D textBB = context.resolve(label.centeredBB);
		Rectangle2D bb = null; //GlobalCache.eval(localSpaceTouchable()).getBoundingBox();
		
		return new Point2D.Double(bb.getMinX()
				+ (bb.getWidth() - textBB.getWidth()) * 0.5, bb.getMaxY()
				+ textBB.getHeight() + 0.1);
	}
	
	private final Expression<ColorisableGraphicalContent> labelGraphics = new ExpressionBase<ColorisableGraphicalContent>(){
		@Override
		protected ColorisableGraphicalContent evaluate(EvaluationContext context) {
			final Point2D labelPosition = labelPosition(context);
			final Color labelColor = context.resolve(VisualComponent.this.labelColor);
			final ColorisableGraphicalContent labelGraphics = context.resolve(label.graphics);
			
			return new ColorisableGraphicalContent(){
				@Override
				public void draw(DrawRequest r) {
					Graphics2D g = r.getGraphics();
					g.setColor(Coloriser.colorise(labelColor, r.getColorisation().getColorisation()));
					AffineTransform transform = g.getTransform();
					g.translate(labelPosition.getX(), labelPosition.getX());
					labelGraphics.draw(r);
					g.setTransform(transform);
				}
			};
		}
	};
	
	protected Expression<ColorisableGraphicalContent> labelGraphics(){
		return labelGraphics;
	}
	
	public ModifiableExpression<Color> labelColor() {
		return labelColor;
	}

	public ModifiableExpression<Color> foregroundColor() {
		return foregroundColor;
	}

	public ModifiableExpression<Color> fillColor() {
		return fillColor;
	}

	public MathNode getReferencedComponent() {
		return refNode;
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		ArrayList<MathNode> result = new ArrayList<MathNode>();
		MathNode refNode = getReferencedComponent();
		if(refNode != null)
			result.add(refNode);
		return result;
	}
}