package org.workcraft.relational.petrinet.modifiable;

import java.awt.geom.AffineTransform;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.relational.petrinet.generated.VisualPetriNetData;
import org.workcraft.relational.petrinet.generated.VisualPlaceData;
import org.workcraft.relational.petrinet.generated.VisualPlaceId;
import org.workcraft.relational.petrinet.generated.VisualTransformableId;
import org.workcraft.util.Func;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class ModifiableVisualPetriNetData {
	public ModifiableVisualPetriNetData(ModifiableExpression<VisualPetriNetData> data) {
		this.data = data;
		
		this.visualPlace = new ModifiableMapImpl(data, new Func<VisualPetriNetData, Map<VisualPlaceId, VisualPlaceData>>() {

			@Override
			public Map<VisualPlaceId, VisualPlaceData> eval(
					VisualPetriNetData arg) {
				return arg.visualPlace;
			}
			
		}));
	}
	
	final ModifiableExpression<VisualPetriNetData> data;
	
	public VisualPlaceId createPlace(VisualPlaceData data) {
		VisualPlaceId result = new VisualPlaceId();
		visualPlace.put(result, data);
		return result;
	}
	
	final ModifiableExpression<Map<VisualPlaceId, VisualPlaceData>> visualPlaceRaw = new ModifiableExpressionImpl<Map<VisualPlaceId,VisualPlaceData>>() {

		@Override
		protected void simpleSetValue(
				Map<VisualPlaceId, VisualPlaceData> newValue) {
			data.setValue(data.withPlaces(newValue));
		}

		@Override
		protected Map<VisualPlaceId, VisualPlaceData> evaluate(EvaluationContext context) {
			return context.resolve(data).visualPlace;
		}
	};
}
