package org.workcraft.relational.engine;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.eval;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.relational.interfaces.DeletionPolicy;
import org.workcraft.relational.interfaces.Field;
import org.workcraft.relational.interfaces.ObjectDeclaration;
import org.workcraft.relational.interfaces.PrimitiveField;
import org.workcraft.relational.interfaces.Relation;
import org.workcraft.relational.interfaces.Relation.Instance;
import org.workcraft.relational.interfaces.RelationField;

import pcollections.HashTreePMap;
import pcollections.PVector;
import pcollections.TreePVector;

public class DatabaseEngineTests {

	private TreePVector<Instance> singleVoidTableSchema() {
		return TreePVector.singleton(new Relation.Instance(new ObjectDeclaration.Instance("table1"), HashTreePMap.<String,Field>empty(), TreePVector.<PVector<String>>empty()));
	}
	
	@Test
	public void testSingleVoidTableDefaultEmpty() {
		DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		Database db = eval(engine.database());
		checkSingleVoidTableDefaultEmpty(db);
	}

	private void checkSingleVoidTableDefaultEmpty(final Database db) {
		Collection<Id> table1List = db.list("table1");
		Assert.assertNotNull(table1List);
		Assert.assertTrue(table1List.isEmpty());
		
		assertFailure(new Runnable(){@Override public void run(){db.list("nonExistingTable");}});
	}

	void assertFailure(Runnable r) {
		boolean testFailed = false;
		try { r.run(); testFailed = true; } catch(Throwable t) {}
		Assert.assertFalse("the task was supposed to fail, but it succeeded :(", testFailed);
	}
	
	@Test
	public void testSingleVoidTableAddToWrongTable() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		assertFailure(new Runnable(){@Override public void run(){engine.add("table2", HashTreePMap.<String,Object>empty());}});
	}
	
	@Test
	public void testSingleVoidTableSimpleAddThenDeleteFromWrongTable() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		final Id addedKey = engine.add("table1", HashTreePMap.<String,Object>empty());
		assertFailure(new Runnable(){@Override public void run(){engine.delete("table2", addedKey);}});
	}

	@Test
	public void testSingleVoidTableSimpleAddThenDeleteNonExistingObject() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		engine.add("table1", HashTreePMap.<String,Object>empty());
		assertFailure(new Runnable(){@Override public void run(){engine.delete("table1", new Id());}});
	}

	@Test
	public void testSingleVoidTableSimpleAddThenDelete() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		final Id addedKey = engine.add("table1", HashTreePMap.<String,Object>empty());
		engine.delete("table1", addedKey);
		Database dbRemoved = eval(engine.database());
		
		checkSingleVoidTableDefaultEmpty(dbRemoved);
	}

	@Test
	public void testSingleVoidTableSimpleAdd() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		final Id addedKey = engine.add("table1", HashTreePMap.<String,Object>empty());
		checkSingleVoidTableSimpleAdd(addedKey, eval(engine.database()));
	}

	@Test
	public void testSingleVoidTableAddBadData() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		assertFailure(new Runnable(){@Override public void run(){engine.add("table1", HashTreePMap.singleton("badField", 8));}});
	}

	@Test
	public void testSingleVoidTableSimplePersistenseAfterAddAndRemove() {
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(singleVoidTableSchema());
		Database dbInit = eval(engine.database());
		final Id addedKey = engine.add("table1", HashTreePMap.<String,Object>empty());
		Database dbAdded = eval(engine.database());
		engine.delete("table1", addedKey);
		Database dbRemoved = eval(engine.database());
		checkSingleVoidTableDefaultEmpty(dbInit);
		checkSingleVoidTableSimpleAdd(addedKey, dbAdded);
		checkSingleVoidTableDefaultEmpty(dbRemoved);
	}

	private void checkSingleVoidTableSimpleAdd(final Id addedKey,
			Database dbAdded) {
		Collection<Id> table1List = dbAdded.list("table1");
		Assert.assertNotNull(table1List);
		Assert.assertFalse("the table must contain the just-added element", table1List.isEmpty());
		Assert.assertEquals(1, table1List.size());
		Assert.assertTrue(table1List.contains(addedKey));
	}

	@Test
	public void testNoTables() {
		DatabaseEngineImpl engine = new DatabaseEngineImpl(TreePVector.<Relation>empty());
		Database db = eval(engine.database());
		
		boolean testFailed = false;
		try { db.list("table1"); testFailed = true; } catch(Throwable t) {}
		try { db.get("table1", "field1", new Id()); testFailed = true; } catch(Throwable t) {}
		Assert.assertFalse(testFailed);
	}

	@Test
	public void testSimpleTableInit() {
		PVector<? extends Relation> schema = simpleTableSchema();
		DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		Database db = eval(engine.database());
		
		checkSingleVoidTableDefaultEmpty(db);
	}

	@Test
	public void testSimpleTableAddUnitialised() {
		PVector<? extends Relation> schema = simpleTableSchema();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		assertFailure(new Runnable(){@Override public void run(){ engine.add("table1", HashTreePMap.<String,Object>empty()); }});
	}

	@Test
	public void testSimpleTableAddBadType() {
		PVector<? extends Relation> schema = simpleTableSchema();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		assertFailure(new Runnable(){@Override public void run(){ engine.add("table1", HashTreePMap.singleton("field1", (Double)8.8)); }});
	}

	@Test
	public void testSimpleTableAddAndGet() {
		PVector<? extends Relation> schema = simpleTableSchema();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		Id addedKey = engine.add("table1", HashTreePMap.singleton("field1", (Integer)8));
		Assert.assertEquals((Integer)8, eval(engine.database()).get("table1", "field1", addedKey));
	}

	private TreePVector<Instance> simpleTableSchema() {
		return TreePVector.singleton(new Relation.Instance(new ObjectDeclaration.Instance("table1"),  
				HashTreePMap.singleton("field1", PrimitiveField.Instance.create(Integer.class)), 
				TreePVector.<PVector<String>>empty()));
	}

	@Test
	public void testReferentialTableSchemaWithCascadeDeletionAddBadRef() {
		TreePVector<Instance> schema = referentialTableSchemaWithCascadeDeletion();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		assertFailure(new Runnable(){@Override public void run(){ engine.add("child", HashTreePMap.singleton("parentRef", new Id())); }});
	}
	
	@Test
	public void testReferentialTableSchemaWithCascadeDeletionAddGoodRef() {
		TreePVector<Instance> schema = referentialTableSchemaWithCascadeDeletion();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		Id parentRecord = engine.add("parent", HashTreePMap.singleton("field1", 8));
		engine.add("child", HashTreePMap.singleton("parentRef", parentRecord));
	}
	
	@Test
	public void testReferentialTableSchemaWithCascadeDeletionAddGoodRefThenDeleteParent() {
		TreePVector<Instance> schema = referentialTableSchemaWithCascadeDeletion();
		final DatabaseEngineImpl engine = new DatabaseEngineImpl(schema);
		Id parentRecord = engine.add("parent", HashTreePMap.singleton("field1", 8));
		engine.add("child", HashTreePMap.singleton("parentRef", parentRecord));
		engine.delete("parent", parentRecord);
		Assert.assertEquals(0, eval(engine.database()).list("child").size());
	}
	
	private TreePVector<Instance> referentialTableSchemaWithCascadeDeletion() {
		ObjectDeclaration.Instance parentDeclaration = new ObjectDeclaration.Instance("parent");
		return TreePVector.singleton(
				new Relation.Instance(parentDeclaration,  
				HashTreePMap.singleton("field1", PrimitiveField.Instance.create(Integer.class)), 
				TreePVector.<PVector<String>>empty())).plus(
					new Relation.Instance(new ObjectDeclaration.Instance("child"), 
							HashTreePMap.singleton("parentRef", new RelationField.Instance(parentDeclaration, DeletionPolicy.CASCADE_DELETE)), 
							TreePVector.<PVector<String>>empty())
				);
	}
}
