package org.workcraft.relational.petrinet.model;

import static org.workcraft.dependencymanager.advanced.core.Expressions.constant;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.HierarchicalGraphicalContent;
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
import pcollections.TreePVector;

public class VisualModel implements org.workcraft.dom.visual.VisualModel {

	public VisualModel() {
		data = new DatabaseEngineImpl(RelationalPetriNet.createSchema().getSchema());
		Id rootVNode = data.add("visualNode", HashTreePMap.<String,Object>empty().plus("parent", null).plus("transform", new AffineTransform()));
		rootVisualGroup = data.add("visualGroup", HashTreePMap.<String,Object>empty().plus("visualNode", rootVNode));
		root = TypeUnsafePetriNetWrapper.wrapVisualGroup(data, rootVisualGroup);
		currentLevel = root;
		
		System.out.println("root visual group id: " + rootVNode);
		
		createVisualPlace(new Point2D.Double(3, 3), Color.RED);
		createVisualTransition(new Point2D.Double(0, 0));
		createVisualPlace(new Point2D.Double(3, 0), Color.GREEN);
		createVisualTransition(new Point2D.Double(0, 3));
	}
	
	final DatabaseEngine data;
	final Id rootVisualGroup;
	final VisualGroupNode root; 
	
	LinkedHashSet<Node> selection = new LinkedHashSet<Node>();
	private VisualGroupNode currentLevel;
	
	@Override
	public void addToSelection(Collection<Node> node) {
		selection.addAll(node);
	}

	@Override
	public void addToSelection(Node node) {
		selection.add(node);
	}
	
	public Id createVisualPlace(Point2D point, Color tokenColor) {
		Id mathPlace = data.add("place", HashTreePMap.<String, Object>empty().plus("initialMarking", 0).plus("name", "p0"));
		Id visualNode = data.add("visualNode", HashTreePMap.<String, Object>empty().plus("transform", AffineTransform.getTranslateInstance(point.getX(), point.getY())).plus("parent", currentLevel.getVisualGroupId()));
		return data.add("visualPlace", HashTreePMap.<String, Object>empty().plus("tokenColor", tokenColor).plus("visualNode", visualNode).plus("mathNode", mathPlace));
	}

	public Id createVisualTransition(Point2D point) {
		Id mathTransition = data.add("transition", HashTreePMap.<String, Object>empty().plus("name", "t0"));
		Id visualNode = data.add("visualNode", HashTreePMap.<String, Object>empty().plus("transform", AffineTransform.getTranslateInstance(point.getX(), point.getY())).plus("parent", currentLevel.getVisualGroupId()));
		return data.add("visualTransition", HashTreePMap.<String, Object>empty().plus("visualNode", visualNode).plus("mathNode", mathTransition));
	}

	private Point2D transformToCurrentSpace(Point2D pointInRootSpace)
	{
		Point2D newPoint = new Point2D.Double();
		TransformHelper.getTransform(getRoot(), currentLevel).transform(pointInRootSpace, newPoint);
		return newPoint;
	}

	@Override
	public Collection<Node> boxHitTest(Point2D p1, Point2D p2) {
		p1 = transformToCurrentSpace(p1);
		p2 = transformToCurrentSpace(p2);
		return HitMan.boxHitTest(currentLevel, p1, p2);
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
	public Container getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public MathModel getMathModel() {
		return null;
	}

	@Override
	public Collection<Node> getSelection() {
		return selection;
	}

	@Override
	public ExpressionBase<HierarchicalGraphicalContent> graphicalContent() {
		return new DrawMan(this).graphicalContent(getRoot());
	}

	@Override
	public void groupSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFromSelection(Node node) {
		selection.remove(node);
	}

	@Override
	public void removeFromSelection(Collection<Node> nodes) {
		selection.remove(nodes);
	}

	@Override
	public void select(Node node) {
		select(TreePVector.singleton(node));
	}

	@Override
	public void select(Collection<Node> newSelection) {
		selection = new LinkedHashSet<Node>(newSelection);
	}

	@Override
	public void selectAll() {
		select(Collections.unmodifiableCollection(eval(currentLevel.children())));
	}

	@Override
	public void selectNone() {
		selection.clear();
	}

	@Override
	public Expression<? extends Collection<? extends Node>> selection() {
		return constant(selection);
	}

	@Override
	public void setCurrentLevel(Container group) {
		currentLevel = (VisualGroupNode) group;
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
	public Node getNodeByReference(String reference) {
		throw new NotSupportedException("new serialisation should be used!");
	}

	@Override
	public String getNodeReference(Node node) {
		throw new NotSupportedException("new serialisation should be used!");
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
	public void remove(Collection<Node> nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ensureConsistency() {
	}

	@Override
	public void add(Container parent, Node node) {
		add(node);
	}

	@Override
	public NodeContext getNodeContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
