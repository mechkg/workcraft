package org.workcraft.util;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dom.visual.ColorisableGraphicalContent.Util.*;

import java.awt.Graphics2D;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.gui.graph.tools.Colorisation;

public class Graphics {
	public static Graphics2D cloneGraphics(Graphics2D g) {
		return (Graphics2D)g.create();
	}
	
	
	public static Function<GraphicalContent, GraphicalContent> statePreserver = new Function<GraphicalContent, GraphicalContent>(){
		@Override
		public GraphicalContent apply(final GraphicalContent content) {
			return new GraphicalContent(){
				@Override
				public void draw(Graphics2D graphics) {
					final Graphics2D g = cloneGraphics(graphics);
					try {
						content.draw(g);
					}
					finally {
						g.dispose();
					}
				}
			};
		}
	};
	
	public static Expression<? extends GraphicalContent> statePreserving(final Expression<? extends GraphicalContent> content) {
		return fmap(statePreserver, content);
	}
	
	public static ColorisableGraphicalContent compose(final ColorisableGraphicalContent bottom, final ColorisableGraphicalContent top) {
		return fromFunc(new Function<Colorisation, GraphicalContent>() {
			@Override
			public GraphicalContent apply(Colorisation colour) {
				return compose(applyColourisation(bottom, colour), applyColourisation(top, colour));
			}
		});
	}

	public static GraphicalContent compose(final GraphicalContent bottom, final GraphicalContent top) {
		return new GraphicalContent(){
			@Override
			public void draw(Graphics2D graphics) {
				Graphics2D clonedGraphics = cloneGraphics(graphics);
				try {
					bottom.draw(clonedGraphics);
					top.draw(graphics);
				}
				finally {
					clonedGraphics.dispose();
				}
			}
		};
	}
	
	public static final Function2<GraphicalContent, GraphicalContent, GraphicalContent> composeFunc = new Function2<GraphicalContent, GraphicalContent, GraphicalContent>(){
		@Override
		public GraphicalContent apply(GraphicalContent bottom, GraphicalContent top) {
			return compose(bottom, top);
		}
	};

	public static Function2<ColorisableGraphicalContent, ColorisableGraphicalContent, ColorisableGraphicalContent> composeColorisable = new Function2<ColorisableGraphicalContent, ColorisableGraphicalContent, ColorisableGraphicalContent>(){
		@Override
		public ColorisableGraphicalContent apply(ColorisableGraphicalContent bottom, ColorisableGraphicalContent top) {
			return compose(bottom, top);
		}
	};

	public static Expression<GraphicalContent> compose(final Expression<? extends GraphicalContent> bottom, final Expression<? extends GraphicalContent> top) {
		return fmap(composeFunc, bottom, top);
	}
}
