package org.workcraft.relational.interfaces;

import pcollections.PMap;
import pcollections.PVector;


public interface Relation {
	PVector<? extends PVector<? extends String>> uniqueIndices();
	PMap<String, ? extends Field> getFields();
	ObjectDeclaration getObjectDeclaration();

	public class Instance implements Relation {

		private final ObjectDeclaration objectDeclaration;
		private final PMap<String, ? extends Field> fields;
		private final PVector<? extends PVector<? extends String>> uniqueKeys;

		public Instance(ObjectDeclaration objectDeclaration,
				PMap<String, ? extends Field> fields,
				PVector<? extends PVector<? extends String>> uniqueKeys) {
			this.objectDeclaration = objectDeclaration;
			this.fields = fields;
			this.uniqueKeys = uniqueKeys;
			
			for(PVector<? extends String> key : uniqueKeys)
				for(String fieldRef : key)
					if(!fields.containsKey(fieldRef))
						throw new RuntimeException("Invalid unique key: non-existing field: '" + fieldRef + "'");
		}

		@Override
		public PMap<String, ? extends Field> getFields() {
			return fields;
		}

		@Override
		public ObjectDeclaration getObjectDeclaration() {
			return objectDeclaration;
		}

		@Override
		public PVector<? extends PVector<? extends String>> uniqueIndices() {
			return uniqueKeys;
		}
	}

}