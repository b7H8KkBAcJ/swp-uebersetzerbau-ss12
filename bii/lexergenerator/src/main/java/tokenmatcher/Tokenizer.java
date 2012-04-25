/*
 * 
 * Copyright 2012 lexergen.
 * This file is part of lexergen.
 * 
 * lexergen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * lexergen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with lexergen.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * lexergen:
 * A tool to chunk source code into tokens for further processing in a compiler chain.
 * 
 * Projectgroup: bi, bii
 * 
 * Authors: Johannes Dahlke
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package tokenmatcher;

import bufferedreader.EndOfFileException;
import bufferedreader.LexemeReader;
import bufferedreader.LexemeReaderException;
import bufferedreader.SpecialChars;


public class Tokenizer implements LexerToParserInterface {

	private DeterministicFiniteAutomata<Character, StatePayload> dfa;

	private LexemeReader lexemeReader;

	private int currentLine = 1;
	private int currentPositionInLine = 0;
	private int lastLine = 1;
	private int lastPositionInLine = 0;
	


	public Tokenizer( LexemeReader lexemeReader,
			DeterministicFiniteAutomata<Character, StatePayload> dfa)
			throws Exception {
		super();
		this.dfa = dfa;
		this.lexemeReader = lexemeReader;
	}
	
	

	public Token getNextToken() throws EndOfFileException, LexemeReaderException,
			LexemIdentificationException {
		Character currentChar;
		String currentLexem = "";
		
		dfa.resetToInitialState();

		while ( true) {
			currentChar = lexemeReader.getNextChar();
      currentPositionInLine++;
			
			// handle white spaces
			if ( currentLexem.isEmpty()
			// Nur wenn nicht bereits ein Lexem verarbeitet wird.
			// Soll ermöglichen, dass auch ein Zeichen über das Ende des zu lesenden
			// Lexem gelesen werden kann, auch wenn es ein whitespace ist.
					&& SpecialChars.isWhiteSpace( currentChar)) {

				// count newlines
				// bei windowssystemen muss dann am Ende durch 2 geteilt werden ,
				// wegen \r\n
				if( SpecialChars.isNewLine( currentChar)) {
					currentPositionInLine++;
					currentPositionInLine = 0;
				}

				// skip whitespaces
				continue;
			}

			
			if ( dfa.canChangeStateByElement( currentChar)) {
				currentLexem += currentChar;
				dfa.changeStateByElement( currentChar);
			} else if ( dfa.getCurrentState().isFiniteState()) {

				StatePayload payload = dfa.getCurrentState().getPayload();

				// Korregieren, was zuviel gelesen wurde.
				currentLexem = currentLexem.substring( 0, currentLexem.length()
						- payload.getBacksteps());
				if ( currentChar != SpecialChars.CHAR_EOF)
				  lexemeReader.stepBackward( payload.getBacksteps() +1);

				// Token erstellen
				TokenType tokenType = payload.getTokenType();
				// TODO: convert lexem to corresponding value
				Token recognisedToken = new Token( tokenType, currentLexem, currentLine, currentPositionInLine);

				// gelesenenes Lexem akzeptieren
				lexemeReader.accept();
				// update position counter
				lastLine = currentLine;
				lastPositionInLine = currentPositionInLine;

				return recognisedToken;
			} else if ( currentChar == SpecialChars.CHAR_EOF) {
				throw new EndOfFileException();
		  } else {
        
				// TODO: Fehlerbehandlung implementieren
				throw new LexemIdentificationException( String.format(
						"Cannot assign lexem %s in line %d at position %d to a token.", currentLexem +currentChar,
						currentLine, currentPositionInLine));
			}

			
		}

	}

}
