package org.workcraft.plugins.workflow;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;

@DisplayName("Node")
public class VisualWorkflowNode extends VisualComponent {
	private Rectangle2D shape = new Rectangle2D.Double(-1,-1,1,1);
	
	public VisualWorkflowNode(WorkflowNode node, StorageManager storage)
	{
		super(node, storage);
	}
	
	@Override
	public Expression<? extends Touchable> localSpaceTouchable() {
		return Expressions.constant(new Touchable(){
			@Override
			public boolean hitTest(Point2D pointInLocalSpace) {
				return shape.contains(pointInLocalSpace);
			}

			@Override
			public Rectangle2D getBoundingBox() {
				return shape;
			}
			
			@Override
			public Point2D getCenter() {
				return new Point2D.Double(0,0);
			}
		});
	}

	@Override
	public Expression<? extends GraphicalContent> graphicalContent() {
		return Expressions.constant(new GraphicalContent() {
			
			@Override
			public void draw(DrawRequest r) {
				r.getGraphics().setColor(Color.BLACK);
				r.getGraphics().setStroke(new BasicStroke(0.01f));
				r.getGraphics().draw(shape);
			}
		});
	}

}
