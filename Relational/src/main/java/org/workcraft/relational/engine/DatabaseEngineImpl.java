package org.workcraft.relational.engine;

import java.util.Collection;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.relational.interfaces.DeletionPolicy;
import org.workcraft.relational.interfaces.Field;
import org.workcraft.relational.interfaces.FieldVisitor;
import org.workcraft.relational.interfaces.PrimitiveField;
import org.workcraft.relational.interfaces.Relation;
import org.workcraft.relational.interfaces.RelationField;
import org.workcraft.util.Null;
import org.workcraft.util.Pair;

import pcollections.HashTreePMap;
import pcollections.PMap;
import pcollections.PVector;
import pcollections.TreePVector;

public class DatabaseEngineImpl implements DatabaseEngine {

	static class DatabaseOperations {
		private final PVector<? extends Relation> schema;
		private final PMap<String, PMap<String, ? extends Field>> fields;

		public DatabaseOperations(PVector<? extends Relation> schema) {
			this.schema = schema;
			PMap<String, PMap<String, ? extends Field>> fields = HashTreePMap.empty();
			for(Relation r : schema) {
				fields = fields.plus(r.getObjectDeclaration().name(), r.getFields());
			}
			this.fields = fields;
			this.dependencies = createDependencies(schema);
		}
		
		private static PMap<String, PVector<Dependency>> createDependencies(PVector<? extends Relation> schema) {
			PMap<String, PVector<Dependency>> result = HashTreePMap.<String, PVector<Dependency>>empty();
			for(final Relation r : schema)
				result = result.plus(r.getObjectDeclaration().name(), TreePVector.<DatabaseEngineImpl.DatabaseOperations.Dependency>empty());
			for(final Relation r : schema) {
				for(final String fieldName : r.getFields().keySet()) {
					Pair<String, Dependency> dep = r.getFields().get(fieldName).accept(new FieldVisitor<Pair<String, Dependency>>() {
						@Override
						public Pair<String, Dependency> visit(RelationField f) {
							return Pair.of(f.getRelation().name(), 
									new Dependency(r.getObjectDeclaration().name(), fieldName, f.getDeletionPolicy()));
						}

						@Override
						public Pair<String, Dependency> visit(PrimitiveField<?> f) {
							return null;
						}
					});
					
					if(dep != null)
						result = result.plus(dep.getFirst(), result.get(dep.getFirst()).plus(dep.getSecond()));
				}
			}
			return result;
		}

		private void validateData(DatabaseImpl dbImpl, String obj, PMap<String, ? extends Object> data) {
			PMap<String, ? extends Field> objFields = fields.get(obj);
			
			PMap<String, ? extends Object> badData = data.minusAll(objFields.keySet());
			PMap<String, ? extends Field> uninitialisedFields = objFields.minusAll(data.keySet());
			if(uninitialisedFields.size() != 0)
				throw new RuntimeException(uninitialisedFields.size()+" fields were not initialised: " + printFieldList(uninitialisedFields.keySet()));
			if(badData.size() != 0)
				throw new RuntimeException(badData.size()+" data for unknown fields found: " + printFieldList(badData.keySet()));
			for(String key : objFields.keySet()) {
				validateFieldValue(dbImpl, key, objFields.get(key), data.get(key));
			}
		}

		private void validateFieldValue(final DatabaseImpl dbImpl, final String fieldName, Field field, final Object value) {
			
			Class<?> expectedType = field.accept(new FieldVisitor<Class<?>>() {
				@Override
				public Class<?> visit(RelationField f) {
					return Id.class;
				}

				@Override
				public Class<?> visit(PrimitiveField<?> f) {
					return f.getType();
				}
			});
			
			if(value == null)  
				return; // all fields are nullable for now
				
			Class<? extends Object> actualType = value.getClass();
			if(!expectedType.isAssignableFrom(actualType))
				throw new RuntimeException(String.format("Incompatible types for field %s: '%s' when '%s' was expected", fieldName, actualType, expectedType));

			
			field.accept(new FieldVisitor<Null>() {
				@Override
				public Null visit(RelationField f) {
					if(!dbImpl.data.get(f.getRelation().name()).containsKey(((Id) value)))
						throw new RuntimeException(String.format("Field %s: can't add a reference to non-existing object", fieldName));
					return null;
				}

				@Override
				public Null visit(PrimitiveField<?> f) {
					return null;
				}
			});
		}

		private String printFieldList(Collection<String> uninitialisedFields) {
			StringBuilder result = new StringBuilder();
			boolean first = true;
			
			for(String s : uninitialisedFields) {
				if(first)
					first = false;
				else
					result.append(", ");
				result.append("'");
				result.append(s);
				result.append("'");
			}
			return result.toString();
		}
		
		static class Dependency {
			public Dependency(String obj,
			String field,
			DeletionPolicy policy) {
				this.obj = obj;
				this.field = field;
				this.policy = policy;
			}
			String obj;
			String field;
			DeletionPolicy policy;
		}
		
		public DatabaseImpl delete(DatabaseImpl db, String obj, Id id) {
			//TODO: handle infinite recursion condition
			for(Dependency dependency : getDependencies(obj)) {
				// TODO: optimise with reverse indices
				PVector<Id> referencingIds = TreePVector.empty();
				for(Id dependantCandidateId : db.list(dependency.obj)) { 
					if(db.get(dependency.obj, dependency.field, dependantCandidateId) == id)
						referencingIds = referencingIds.plus(dependantCandidateId);
				}
				if(dependency.policy == DeletionPolicy.CASCADE_DELETE)
					for(Id dependantId : referencingIds) {// TODO: think about deletion making this list obsolete
						db = delete(db, dependency.obj, dependantId);
					}
				else
					throw new NotImplementedException();
			}
			
			db = simpleDelete(db, obj, id);
			
			return db;
		}
		
		private DatabaseImpl simpleDelete(DatabaseImpl db, String obj, Id id) {
			PMap<String, PMap<Id, PMap<String, ? extends Object>>> oldData = db.data;
			PMap<Id, PMap<String,? extends Object>> objectTable = oldData.get(obj);
			if(!objectTable.containsKey(id))
				throw new RuntimeException("cannot delete: object not found");
			return new DatabaseImpl(oldData.plus(obj, objectTable.minus(id)));
		}

		PMap<String, PVector<Dependency>> dependencies;

		private PVector<Dependency> getDependencies(String obj) {
			return dependencies.get(obj);
		}
	}
	
	public DatabaseEngineImpl(PVector<? extends Relation> schema) {
		db = new Variable<DatabaseImpl>(new DatabaseImpl(schema));
		operations = new DatabaseOperations(schema);
	}
	
	private final DatabaseOperations operations;
	private final Variable<DatabaseImpl> db;

	@Override
	public Expression<? extends Database> database() {
		return db;
	}

	@SuppressWarnings("unchecked")
	static <K, VN> PMap<K, VN> plus(PMap<K, ? extends VN> m1, K k2, VN v2) {
		return ((PMap<K,VN>)m1).plus(k2, v2);
	}
	
	interface MyMap<K, V> {
		K getK();
		V get(K k);
	}
	
	@Override
	public void delete(String obj, Id id) {
		DatabaseImpl dbImpl = db.getValue();
		
		db.setValue(operations.delete(dbImpl, obj, id));
	}

	@Override
	public Id add(String obj, PMap<String, ? extends Object> data) {
		DatabaseImpl dbImpl = db.getValue();
		
		operations.validateData(dbImpl, obj, data);
		Id newObj = new Id();
		PMap<String, PMap<Id, PMap<String, ? extends Object>>> oldData = dbImpl.data;
		PMap<String, PMap<Id, PMap<String, ? extends Object>>> newData = oldData.plus(obj, oldData.get(obj).plus(newObj, data));
		db.setValue(new DatabaseImpl(newData));
		
		return newObj;
	}

	@Override
	public void setValue(String object, String fieldName, Id id, Object newValue) {
		DatabaseImpl old = db.getValue();
		PMap<Id, PMap<String, ? extends Object>> table = old.data.get(object);
		PMap<String, ? extends Object> record = table.get(id);
		// TODO: handle bad cases
		db.setValue(new DatabaseImpl(old.data.plus(object, table.plus(id, plus(record, fieldName, newValue)))));
	}

}
