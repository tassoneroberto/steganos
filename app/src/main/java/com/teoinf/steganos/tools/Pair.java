package com.teoinf.steganos.tools;

public class Pair<T, U> {

	private T _first;
	private U _second;
	
	public Pair() {	
	}
	
	public Pair(T first, U second) {
		_first = first;
		_second = second;
	}
	
	public T getFirst() {
		return _first;
	}
	
	public void setFirst(T first) {
		_first = first;
	}
	
	public U getSecond() {
		return _second;
	}
	
	public void setSecond(U second) {
		_second = second;
	}
}
