package semantic.analysis.AstNodes;

import lexer.IToken.TokenType;
import lombok.AllArgsConstructor;
import semantic.analysis.SymbolTableStack;

@AllArgsConstructor
public class BasicType extends Type {
		
	TokenType type;
	
	public void run(SymbolTableStack tables) {

	}
}