package br.com.ooboo.asm.defuse;

import java.util.Set;

import org.objectweb.asm.Type;

public class ArrayLength extends Value {

	public final Value value;

	public ArrayLength(final Value value) {
		super(Type.INT_TYPE);
		this.value = value;
	}

	@Override
	public Set<Variable> getVariables() {
		return value.getVariables();
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(), value);
	}

}
