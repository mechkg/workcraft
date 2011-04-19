package org.workcraft.dom.visual;

import java.awt.Graphics2D;

import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;

public interface ColorisableGraphicalContent {
	ColorisableGraphicalContent EMPTY = new ColorisableGraphicalContent(){
		@Override
		public void draw(DrawRequest request) {
		}
	};
	
	public class Util {
		public static ColorisableGraphicalContent fromFunc(final Function<Colorisation, GraphicalContent> func) {
			return new ColorisableGraphicalContent() {
				@Override
				public void draw(DrawRequest request) {
					func.apply(request.getColorisation()).draw(request.getGraphics());
				}
			};
		}
		
		public static GraphicalContent applyColourisation(final ColorisableGraphicalContent content, final Colorisation colour) {
			return new GraphicalContent() {
				
				@Override
				public void draw(final Graphics2D graphics) {
					content.draw(new DrawRequest() {
						@Override
						public Graphics2D getGraphics() {
							return graphics;
						}
						
						@Override
						public Colorisation getColorisation() {
							return colour;
						}
					});
				}
			};
		}
		public final static Function2<ColorisableGraphicalContent, Colorisation, GraphicalContent> applyColourisationFunc = new Function2<ColorisableGraphicalContent, Colorisation, GraphicalContent>() {
			@Override
			public GraphicalContent apply(ColorisableGraphicalContent argument1, Colorisation argument2) {
				return applyColourisation(argument1, argument2);
			}
		};
	}

	public void draw(DrawRequest request);
}
