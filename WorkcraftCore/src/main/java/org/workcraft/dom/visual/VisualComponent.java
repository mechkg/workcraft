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
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.ExpressionPropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements DependentNode, DrawableNew {
	private final MathNode refNode;

	private static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1)
			.deriveFont(0.5f);

	public static FontRenderContext podgonFontRenderContext() {
		return new FontRenderContext(AffineTransform.getScaleInstance(1000, 1000), true, true);
	}
	
	private Variable<String> label = Variable.create("");
	private Expression<GlyphVector> labelGlyphs = new ExpressionBase<GlyphVector>(){
		@Override
		protected GlyphVector evaluate(EvaluationContext context) {
			return labelFont.createGlyphVector(podgonFontRenderContext(), context.resolve(label));
		}
	};

	private Variable<Color> labelColor = Variable.create(CommonVisualSettings.getForegroundColor());
	private Variable<Color> foregroundColor = Variable.create(CommonVisualSettings.getForegroundColor());
	private Variable<Color> fillColor = Variable.create(CommonVisualSettings.getFillColor());

	private void addPropertyDeclarations() {
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label", label(), String.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Label color", labelColor(), Color.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Foreground color", foregroundColor(), Color.class));
		addPropertyDeclaration(ExpressionPropertyDeclaration.create("Fill color", fillColor(), Color.class));
	}

	public VisualComponent(MathNode refNode) {
		super();
		this.refNode = refNode;

		addPropertyDeclarations();
	}
	public ModifiableExpression<String> label() {
		return label;
	}

	public Expression<Rectangle2D> getLabelBB() {
		return new ExpressionBase<Rectangle2D>(){
			@Override
			protected Rectangle2D evaluate(EvaluationContext context) {
				return context.resolve(labelGlyphs).getVisualBounds();
			}
		};
	}

	protected Expression<GraphicalContent> labelGraphics(){
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){
					@Override
					public void draw(DrawRequest r) {
						Rectangle2D textBB = context.resolve(labelGlyphs).getLogicalBounds();
						Rectangle2D bb = GlobalCache.eval(localSpaceTouchable()).getBoundingBox();
						Point2D labelPosition = new Point2D.Double(bb.getMinX()
								+ (bb.getWidth() - textBB.getWidth()) * 0.5, bb.getMaxY()
								+ textBB.getHeight() + 0.1);
						
						r.getGraphics().setColor(Coloriser.colorise(context.resolve(labelColor), r.getDecoration().getColorisation()));
						// g.drawGlyphVector(labelGlyphs, (float)labelPosition.getX(),
						// (float)labelPosition.getY());
						r.getGraphics().setFont(labelFont);
						r.getGraphics().drawString(context.resolve(label), (float) labelPosition.getX(),
								(float) labelPosition.getY());
					}
					
				};
			}
		};
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