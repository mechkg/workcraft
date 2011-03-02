package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface ModifiableExpression<T> extends Expression<T>, Setter<T> {
}
