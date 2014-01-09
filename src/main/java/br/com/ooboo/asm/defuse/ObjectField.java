package br.com.ooboo.asm.defuse;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.Type;

public final class ObjectField extends Field {

	public final Value value;

	public ObjectField(final String owner, final String name, final String desc, final Value value) {
		super(owner, name, desc);
		if (value.type.getSort() != Type.OBJECT) {
			throw new IllegalArgumentException("Invalid value type: " + value.type);
		}
		this.value = value;
	}

	@Override
	public Set<Variable> getVariables() {
		final Set<Variable> values = new LinkedHashSet<Variable>();
		values.addAll(value.getVariables());

		// get root
		Value root = value;
		while (root instanceof ObjectField) {
			root = ObjectField.class.cast(root).value;
		}

		if (root instanceof Local || root instanceof StaticField) {
			values.add(this);
		}

		return Collections.unmodifiableSet(values);
	}

	@Override
	public String toString() {
		return String.format("%s.%s.%s", value, owner.replace("/", "."), name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + value.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!super.equals(obj))
			return false;

		final ObjectField other = (ObjectField) obj;

		if (!value.equals(other.value))
			return false;

		return true;
	}

}
