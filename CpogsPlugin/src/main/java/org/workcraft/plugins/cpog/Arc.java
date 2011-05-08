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

package org.workcraft.plugins.cpog;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.connections.BezierData;
import org.workcraft.dom.visual.connections.ConnectionDataVisitor;
import org.workcraft.dom.visual.connections.VisualConnectionData;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.One;

public interface Arc {
	public ModifiableExpression<BooleanFormula> condition();
	public Vertex first();
	public Vertex second();
	public ModifiableExpression<VisualConnectionData> visual();
	public class Util {
		public static Node asNode(final Arc arc) {
			return new Node() {

				@Override
				public <T> T accept(NodeVisitor<T> visitor) {
					return visitor.visitArc(arc);
				}
				
			};
		}
		public static Arc create(final Vertex first, final Vertex second, final StorageManager storage) {
			VisualConnectionData defaultConnectionData = new VisualConnectionData(){
				ModifiableExpression<Point2D> cp1 = storage.<Point2D>create(new Point2D.Double(1.0/3.0, 0));
				ModifiableExpression<Point2D> cp2 = storage.<Point2D>create(new Point2D.Double(2.0/3.0, 0));
	
				@Override
				public <T> T accept(ConnectionDataVisitor<T> visitor) {
					return visitor.visitBezier(new BezierData() {
						
						@Override
						public ModifiableExpression<Point2D> cp1() {
							return cp1;
						}
	
						@Override
						public ModifiableExpression<Point2D> cp2() {
							return cp2;
						}
					});
				}
				
			};
			final ModifiableExpression<BooleanFormula> condition = storage.<BooleanFormula> create(One.instance());
			final ModifiableExpression<VisualConnectionData> visual = storage.create(defaultConnectionData);
			return new Arc(){
				@Override public Vertex first() { return first; }
				@Override public Vertex second() { return second; }
				@Override public ModifiableExpression<VisualConnectionData> visual() { return visual; }
				@Override public ModifiableExpression<BooleanFormula> condition() { return condition; }
			};
		}
	}
}
