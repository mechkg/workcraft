package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface PickyModifiableExpression<T,S> extends Expression<T>, PickySetter<T,S> {
}
