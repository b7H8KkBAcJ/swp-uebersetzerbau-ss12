package parser.nodes;

import parser.Terminal;

public class OP_COMMA extends Terminal {

	public OP_COMMA(String name) {
		super(name);
	}
	
	@Override
	public void run(){
		// skip
	}
}
