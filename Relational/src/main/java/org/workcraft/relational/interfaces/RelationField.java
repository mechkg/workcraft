package org.workcraft.relational.interfaces;


public interface RelationField {
	public class Instance implements Field, RelationField {

		private final ObjectDeclaration relation;
		private final DeletionPolicy deletionPolicy;

		public Instance(ObjectDeclaration relation, DeletionPolicy deletionPolicy) {
			this.relation = relation;
			this.deletionPolicy = deletionPolicy;
		}

		@Override
		public <T> T accept(FieldVisitor<T> visitor) {
			return visitor.visit(this);
		}

		@Override
		public DeletionPolicy getDeletionPolicy() {
			return deletionPolicy;
		}

		@Override
		public ObjectDeclaration getRelation() {
			return relation;
		}
	}
	ObjectDeclaration getRelation();
	DeletionPolicy getDeletionPolicy();
}