package de.fuberlin.projecta.analysis.ast.nodes;


/**
 * first child num
 * second child Type
 * 
 */
public class ArrayCall extends AbstractSyntaxTree {

	@Override
	public boolean checkSemantics() {
		return true;
	}

	@Override
	public String genCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkTypes() {
		// TODO Auto-generated method stub
		return false;
	}
}
