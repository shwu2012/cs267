package edu.sjsu.cs267.tools;

public class Triple<T1, T2, T3> {
	public T1 first;
	public T2 second;
	public T3 third;

	private Triple() {
	}

	public static <A, B, C> Triple<A, B, C> of(A v1, B v2, C v3) {
		Triple<A, B, C> triple = new Triple<A, B, C>();
		triple.first = v1;
		triple.second = v2;
		triple.third = v3;
		return triple;
	}

	@Override
	public String toString() {
		return String.format("{first: %s, second: %s, third: %s}",
				first.toString(), second.toString(), third.toString());
	}
}
