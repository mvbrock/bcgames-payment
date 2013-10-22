package org.mvbrock.bcgames.payment.model;

public class GameDescriptor {
	private String id;
	private String name;
	
	public GameDescriptor() { }
	
	public GameDescriptor(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}
