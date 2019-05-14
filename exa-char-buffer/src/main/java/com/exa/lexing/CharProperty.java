package com.exa.lexing;

public class CharProperty<T> {
	
	private Character character;
	
	private T property;
	
	public CharProperty(Character character, T property) {
		super();
		this.character = character;
		this.property = property;
	}

	public Character getCharacter() {
		return character;
	}

	public T getProperty() {
		return property;
	}

	public void setProperty(T property) {
		this.property = property;
	}

	public void setCharacter(Character character) {
		this.character = character;
	}
	
	

}
