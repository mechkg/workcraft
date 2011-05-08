package org.workcraft.plugins.circuit.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.gui.propertyeditor.EditableProperty;


public interface ComponentRenderingResult {
	
	public enum RenderType {
		BOX, GATE, BUFFER, C_ELEMENT;
		public class EditorProperty {
			public static EditableProperty create(String name, ModifiableExpression<RenderType> property) {
				LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
				types.put("Box", RenderType.BOX);
				types.put("Gate", RenderType.GATE);
				types.put("C-Element", RenderType.C_ELEMENT);
				
				return EditableProperty.Util.create(, renderer, property)
			}
		}
	}
	
	Rectangle2D boundingBox();
	Map<String, Point2D> contactPositions();
	void draw(Graphics2D graphics);	
}
