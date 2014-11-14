package com.doschool.entity;

import java.io.Serializable;

public class SquareEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 585817818666594521L;
	public static final int TYPE_BLOG=1;
	public static final int TYPE_TOPIC_RECOMMEND=2;
	public static final int TYPE_TOPIC_BLOGWALL=3;
	public static final int TYPE_PERSON=4;
	
	
	public int type=TYPE_BLOG;
	public Microblog blog;
	public Topic topic;
	

}
