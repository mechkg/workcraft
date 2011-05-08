package org.workcraft.plugins.circuit;
import java.awt.BasicStroke;
import java.awt.Stroke;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.plugins.petri.Place;

public class VisualCircuitConnection extends VisualConnection {
	private Place referencedZeroPlace=null;
	private Place referencedOnePlace=null;

	public VisualCircuitConnection(StorageManager storage) {
		super(storage);
	}
	
	public VisualCircuitConnection(MathConnection c, StorageManager storage) {
		super(storage);
	}
	
	public VisualCircuitConnection(MathConnection con, VisualComponent c1, VisualComponent c2, StorageManager storage) {
		super(con, c1, c2, storage);
	}

	public void setReferencedZeroPlace(Place referencedPlace) {
		this.referencedZeroPlace = referencedPlace;
	}

	@Override
	public ExpressionBase<VisualConnectionProperties> properties() {
		final ExpressionBase<VisualConnectionProperties> superProperties = super.properties();
		return new ExpressionBase<VisualConnectionProperties>(){
			@Override
			protected VisualConnectionProperties evaluate(EvaluationContext context) {
				return new VisualConnectionProperties.Inheriting(context.resolve(superProperties)){
					@Override
					public Stroke getStroke() {
						return new BasicStroke((float)CircuitSettings.getCircuitWireWidth());
					}
				};
			}
		};
	}
	
	public Place getReferencedZeroPlace() {
		return referencedZeroPlace;
	}
	
	public void setReferencedOnePlace(Place referencedPlace) {
		this.referencedOnePlace = referencedPlace;
	}

	public Place getReferencedOnePlace() {
		return referencedOnePlace;
	}
	
/*	@Override
	public Color getDrawColor()
	{
		if (referencedOnePlace==null||
			referencedZeroPlace==null) return super.getDrawColor(); 
		
		if (referencedOnePlace.getTokens()==1&&
				referencedZeroPlace.getTokens()==0) return Color.RED;
		if (referencedOnePlace.getTokens()==0&&
				referencedZeroPlace.getTokens()==1) return Color.BLUE;
			
		return super.getDrawColor();
	}*/
	
}
