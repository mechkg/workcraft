package org.workcraft.relational.petrinet.model;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HierarchicalGraphicalContent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.relational.engine.DatabaseEngine;
import org.workcraft.relational.engine.DatabaseEngineImpl;
import org.workcraft.relational.petrinet.declaration.RelationalPetriNet;

public class VisualModel implements org.workcraft.dom.visual.VisualModel {

	DatabaseEngine data = new DatabaseEngineImpl(RelationalPetriNet.createSchema().getSchema());
	
	LinkedHashSet<Node> selection = new LinkedHashSet<Node>();
	
	@Override
	public void addToSelection(Collection<Node> node) {
		selection.addAll(node);
	}

	@Override
	public void addToSelection(Node node) {
		selection.add(node);
	}

	@Override
	public Collection<Node> boxHitTest(Point2D p1, Point2D p2) {
		return null;
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		
	}

	@Override
	public void deleteSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Container getCurrentLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MathModel getMathModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExpressionBase<HierarchicalGraphicalContent> graphicalContent() {
		return null;
	}

	@Override
	public void groupSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFromSelection(Node node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFromSelection(Collection<Node> nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void select(Node node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void select(Collection<Node> node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectNone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExpressionBase<? extends Collection<? extends Node>> selection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentLevel(Container group) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNodeByReference(String reference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNodeReference(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getProperties(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
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
	public Set<Connection> getConnections(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Node> getPostset(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Node> getPreset(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

}
