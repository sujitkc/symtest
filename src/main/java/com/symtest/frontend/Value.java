package com.symtest.frontend;

/**
 * Wrapper class used to return values from the visitor functions.
 */
public class Value {

	final Object value;

	public Value(Object value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}

	@Override
	public int hashCode() {
		if (value == null) {
			return 0;
		}
		return this.value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (value == o) {
			return true;
		} 
		if (value == null || o == null || o.getClass() != value.getClass()) {
			return false;
		}

		Value that = (Value) o;
		return this.value.equals(that.value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
