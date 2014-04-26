package edu.sjsu.cs267.tools;

public class Pair<T1, T2> {
	public T1 first;
	public T2 second;

	private Pair() {
	}

	public static <A, B> Pair<A, B> of(A v1, B v2) {
		Pair<A, B> pair = new Pair<A, B>();
		pair.first = v1;
		pair.second = v2;
		return pair;
	}

	@Override
	public String toString() {
		return String.format("{ first: %s, second: %s }", first.toString(),
				second.toString());
	}
}
