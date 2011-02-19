package org.workcraft.relational.petrinet.model;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.DatabaseEngineImpl;
import org.workcraft.relational.engine.Id;
import org.workcraft.relational.petrinet.declaration.RelationalPetriNet;
import org.workcraft.relational.petrinet.typeunsafe.TypeUnsafePetriNetWrapper;

import pcollections.HashTreePMap;
import pcollections.HashTreePSet;
import pcollections.PSet;

public class VisualModel implements org.workcraft.dom.visual.VisualModel {

	public VisualModel() {
		data = new DatabaseEngineImpl(RelationalPetriNet.createSchema().getSchema());
		Id rootVNode = data.add("visualNode", HashTreePMap.<String,Object>empty().plus("parent", null).plus("transform", new AffineTransform()));
		rootVisualGroup = data.add("visualGroup", HashTreePMap.<String,Object>empty().plus("visualNode", rootVNode));
		root = TypeUnsafePetriNetWrapper.wrapVisualGroup(data, rootVisualGroup);
		currentLevel.setValue(root);
		
		System.out.println("root visual group id: " + rootVNode);
		
		createVisualPlace(new Point2D.Double(3, 3), Color.RED);
		createVisualTransition(new Point2D.Double(0, 0));
		createVisualPlace(new Point2D.Double(3, 0), Color.GREEN);
		createVisualTransition(new Point2D.Double(0, 3));
	}
	
	final DatabaseEngine data;
	final Id rootVisualGroup;
	final VisualGroupNode root; 
	
	ModifiableExpression<PSet<Node>> selection = Variable.<PSet<Node>>create(HashTreePSet.<Node>empty());
	private ModifiableExpression<VisualGroupNode> currentLevel = Variable.create(null);
	
	public Id createVisualPlace(Point2D point, Color tokenColor) {
		Id mathPlace = data.add("place", HashTreePMap.<String, Object>empty().plus("initialMarking", 0).plus("name", "p0"));
		Id visualNode = data.add("visualNode", HashTreePMap.<String, Object>empty().plus("transform", AffineTransform.getTranslateInstance(point.getX(), point.getY())).plus("parent", eval(currentLevel).getVisualGroupId()));
		return data.add("visualPlace", HashTreePMap.<String, Object>empty().plus("tokenColor", tokenColor).plus("visualNode", visualNode).plus("mathNode", mathPlace));
	}

	public Id createVisualTransition(Point2D point) {
		Id mathTransition = data.add("transition", HashTreePMap.<String, Object>empty().plus("name", "t0"));
		Id visualNode = data.add("visualNode", HashTreePMap.<String, Object>empty().plus("transform", AffineTransform.getTranslateInstance(point.getX(), point.getY())).plus("parent", eval(currentLevel).getVisualGroupId()));
		return data.add("visualTransition", HashTreePMap.<String, Object>empty().plus("visualNode", visualNode).plus("mathNode", mathTransition));
	}

	private Point2D transformToCurrentSpace(Point2D pointInRootSpace)
	{
		Point2D newPoint = new Point2D.Double();
		TransformHelper.getTransform(getRoot(), eval(currentLevel)).transform(pointInRootSpace, newPoint);
		return newPoint;
	}

	@Override
	public Collection<Node> boxHitTest(Point2D p1, Point2D p2) {
		p1 = transformToCurrentSpace(p1);
		p2 = transformToCurrentSpace(p2);
		return HitMan.boxHitTest(eval(currentLevel), p1, p2);
	}


	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ModifiableExpression<Container> currentLevel() {
		return Expressions.cast(currentLevel, VisualGroupNode.class, Container.class);
	}

	@Override
	public MathModel getMathModel() {
		return null;
	}

	@Override
	public void groupSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ModifiableExpression<PSet<Node>> selection() {
		return selection;
	}

	@Override
	public void ungroupSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Node node) {
		throw new NotSupportedException();
	}

	@Override
	public Properties getProperties(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getRoot() {
		return root;
	}

	@Override
	public String getTitle() {
		return "some other shit. is this ever used?";
	}

	@Override
	public void remove(Node node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(Collection<? extends Node> nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Container parent, Node node) {
		add(node);
	}

	@Override
	public Expression<? extends ReferenceManager> referenceManager() {
		throw new NotSupportedException();
	}

	@Override
	public Expression<? extends NodeContext> nodeContext() {
		throw new NotSupportedException();
	}
}
