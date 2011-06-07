package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.Expression;
import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import org.workcraft.util.Function;
import org.workcraft.util.Function2;

import pcollections.PVector;

public interface VisualConnectionData {
	class Util {

		public static Expression<StaticVisualConnectionData> getStatic(VisualConnectionData data) {
			return data.accept(new ConnectionDataVisitor<Expression<StaticVisualConnectionData>>() {

				@Override
				public Expression<StaticVisualConnectionData> visitPolyline(PolylineData data) {
					return fmap(new Function<StaticPolylineData, StaticVisualConnectionData>(){
						@Override
						public StaticVisualConnectionData apply(final StaticPolylineData argument) {
							return new StaticVisualConnectionData(){

								@Override
								public <T> T accept(StaticConnectionDataVisitor<T> visitor) {
									return visitor.visitPolyline(argument);
								}
							};
						}
					}, getStatic(data));
				}

				@Override
				public Expression<StaticVisualConnectionData> visitBezier(BezierData data) {
					return fmap(new Function<StaticBezierData, StaticVisualConnectionData>(){
						@Override
						public StaticVisualConnectionData apply(final StaticBezierData argument) {
							return new StaticVisualConnectionData(){

								@Override
								public <T> T accept(StaticConnectionDataVisitor<T> visitor) {
									return visitor.visitBezier(argument);
								}
							};
						}
					}, getStatic(data));
				}
			});
		}

		public static Expression<? extends StaticPolylineData> getStatic(PolylineData data) {
			return fmap(new Function<PVector<Point2D>, StaticPolylineData>(){

				@Override
				public StaticPolylineData apply(final PVector<Point2D> argument) {
					return new StaticPolylineData() {
								
								@Override
								public List<Point2D> controlPoints() {
									return argument;
								}
					};
				}}, joinCollection(data.controlPoints()));
		}

		public static Expression<? extends StaticBezierData> getStatic(BezierData data) {
			return fmap(new Function2<RelativePoint, RelativePoint, StaticBezierData>(){

				@Override
				public StaticBezierData apply(final RelativePoint cp1, final RelativePoint cp2) {
					return new StaticBezierData() {

						@Override
						public RelativePoint cp1() {
							return cp1;
						}

						@Override
						public RelativePoint cp2() {
							return cp2;
						}
					};
				}}, data.cp1(), data.cp2());
		}
	}

	<T> T accept(ConnectionDataVisitor<T> visitor);
}