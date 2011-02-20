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

package org.workcraft.dom.visual.connections;

import java.awt.geom.AffineTransform;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;

public class ComponentsTransformer {
	
	public static ExpressionBase<Touchable> transform (VisualComponent first, Node transformTo) {
		Expression<AffineTransform> expr = TransformHelper.getTransformExpression(Expressions.constant(first), Expressions.constant(transformTo));
		return TransformHelper.transform(first.shape(), expr);

	}
}