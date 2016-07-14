package com.xk.player.lrc;

public class XRCNode {

	public Long start;
	public Long length;
	public String word;
	
	public XRCNode(Long start, Long length, String word) {
		super();
		this.start = start;
		this.length = length;
		this.word = word;
	}
	
	public XRCNode() {
		super();
	}
	
}
