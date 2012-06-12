package de.fuberlin.projectcii.ParserGenerator.src;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.fuberlin.projectcii.ParserGenerator.src.extern.ISyntaxTree;

/**
 * Debugging Class to visualize the current status of the used Datastructures
 * 
 * @author Patrick Schlott
 */
public class Printer {

	
	/**
	 * Use this Function to view the global Grammar inside the grammarReader.
	 * 
	 * @author Patrick Schlott
	 */
	static public void printGrammar(Vector<Productions> grammar){	
		
		for (int i = 0; i< grammar.size() ; i++){
			Productions nonterminal = grammar.elementAt(i);
			System.out.print("Head = "+nonterminal.getHead()+" - ");
			System.out.print("Rump = ");
			int productionNr = nonterminal.productions.size();
			for (int j = 0; j < productionNr; j++){
				Vector<String> production = nonterminal.productions.elementAt(j);
				int symbolNr = production.size();
				for (int k = 0; k < symbolNr; k++){
					String symbol = production.elementAt(k);
					System.out.print(symbol+".");
				}
				System.out.print("|");
			}
			System.out.println();
		}
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the global Grammar inside the ParserGenerator.
	 * 
	 * @author Patrick Schlott
	 */
	
	static public void printGrammar(Map<String, Vector<Vector<String>>> grammar){	
		
		for (String currentHead : grammar.keySet()){
			System.out.print("Head = "+currentHead);
			System.out.print(" - Rump = ");
			for (Vector<String> production : grammar.get(currentHead)) {
				for (String symbol : production) {
					System.out.print(symbol+".");
				}
				System.out.print("|");
			}
			System.out.println();
		}
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the global Grammar inside the ParserGenerator.
	 * 
	 * @author Patrick Schlott
	 */
	
	static public void printProduction(Map<String, Vector<Vector<String>>> grammar,String key, int productionNr){	
		
		System.out.print(key+" -> ");
		
		for (String symbol : grammar.get(key).elementAt(productionNr)){
			System.out.print(symbol+" ");
		}
		System.out.println();
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the Parsetable inside the ParserGenerator.
	 * 
	 * @author Patrick Schlott
	 */
	
	static public void printParserTable(Vector<String> Terminals,
										Vector<String> Nonterminal,
										Map<String, HashMap<String,Vector<Integer>>> parserTable) {
		System.out.print("\t"+"|");
		for (String terminal:Terminals){
			System.out.print(terminal+"\t"+"|");
		}
		
		for (String nonTerminal:Nonterminal){
			System.out.println("");
			for (int i = 0; i<Terminals.size();i++){
				System.out.print("_________");
			}
			System.out.println("");
			System.out.print(nonTerminal+"\t"+"|");
			for (String terminal:Terminals){
				Vector<Integer> productions = (parserTable.get(nonTerminal)).get(terminal);
				if (productions.size() == 0){
					System.out.print(""+"\t"+"|");
				}
				else if (productions.size() == 1){
					System.out.print(productions.elementAt(0)+"\t"+"|");
				}
				else{
					for (int i=0;i<productions.size();i++){
						if (i == productions.size()-1){
							System.out.print(productions.elementAt(i));
						}
						else{
							System.out.print(productions.elementAt(i)+",");
						}
					}
					System.out.print(""+"\t"+"|");
				}
			}
		}
		System.out.println();
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the Parsetable inside the ParserGenerator.
	 * 
	 * @author Patrick Schlott
	 */
	
	static public void printParserTable(Vector<String> Terminals,
										Vector<String> Nonterminal,
										Map<String, HashMap<String,Vector<Integer>>> parserTable,
										Map<String, Vector<Vector<String>>> grammar) {
		System.out.print("\t"+"|");
		for (String terminal:Terminals){
			System.out.print(terminal+"\t"+"|");
		}
		
		for (String nonTerminal:Nonterminal){
			System.out.println("");
			for (int i = 0; i<Terminals.size();i++){
				System.out.print("_________");
			}
			System.out.println("");
			System.out.print(nonTerminal+"\t"+"|");
			for (String terminal:Terminals){
				Vector<Integer> productions = (parserTable.get(nonTerminal)).get(terminal);
				if (productions.size() == 0){
					System.out.print(""+"\t"+"|");
				}
				else if (productions.size() == 1){
					Vector<Vector<String>> x = grammar.get(nonTerminal);
					Vector<String> y = x.elementAt(productions.elementAt(0));
					System.out.print(nonTerminal+" -> ");
					
					for (String z:y){
						System.out.print(z+" ");
					}
					System.out.print("\t"+"|");
				}
				else{
					for (int i=0;i<productions.size();i++){
						if (i == productions.size()-1){
							System.out.print(productions.elementAt(i));
						}
						else{
							System.out.print(productions.elementAt(i)+",");
						}
					}
					System.out.print(""+"\t"+"|");
				}
			}
		}
		System.out.println();
		System.out.println("-----------");
	}
	
	
	/**
	 * Use this Function to view the FirstSet inside the ParserGenerator.
	 * 
	 * @author Ying Wei, Patrick Schlott
	 */
	static public void printFirstSets(Map<String, Set<String>> firstSets) {
		for (Entry<String, Set<String>> fs : firstSets.entrySet()) {
			System.out.println("First(" + fs.getKey() + ") = " + fs.getValue());
		}
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the FirstSet inside the ParserGenerator.
	 * 
	 * @author Ying Wei, Patrick Schlott
	 */
	static public void printFollowSets(Map<String, Set<String>> followSets) {
		for (Entry<String, Set<String>> fs : followSets.entrySet()) {
			System.out.println("Follow(" + fs.getKey() + ") = " + fs.getValue());
		}
		System.out.println("-----------");
	}
	
	/**
	 * Use this Function to view the FirstSetProductions inside the ParserGenerator.
	 * 
	 * @author Christoph Schr�der, Patrick Schlott
	 */
	static public void printFirstSetsProductions(Map<String, HashMap<String,Integer>> firstSetsProductions) {
		for (Entry<String, HashMap<String,Integer>> fs : firstSetsProductions.entrySet()) {
			System.out.println("First(" + fs.getKey() + ") = " + fs.getValue().keySet());
		}
		System.out.println("-----------");
	}
	
	static public void parsetreeToXML(ISyntaxTree node){
		
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement("ParseTree");
			doc.appendChild(root);
			
			for (ISyntaxTree child:node.getChildren()){
				String tagName = child.getSymbol();
				if (tagName.startsWith("<")){
					tagName = tagName.substring(1, tagName.length()-1);
				}
				Element childNode;
				System.out.println(tagName);
				if (child.getChildrenCount() == 0){
					childNode = doc.createElement("LEAF");
					root.appendChild(childNode);
				}
				else{
					childNode = doc.createElement(tagName);
					root.appendChild(childNode);
					doc = childrenToXML(child, childNode, doc);
				}
				
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("parseTree.xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
	  }
	}
	
	static private Document childrenToXML(ISyntaxTree node, Element parentNode, Document doc){
		
		for (ISyntaxTree child:node.getChildren()){
			String tagName = child.getSymbol();
			if (tagName.startsWith("<")){
				tagName = tagName.substring(1, tagName.length()-1);
			}
			System.out.println(tagName);
			Element childNode;
			if (child.getChildrenCount() == 0){
				childNode = doc.createElement("LEAF");
				parentNode.appendChild(childNode);
			}
			else{
				childNode = doc.createElement(tagName);
				doc = childrenToXML(child, childNode, doc);
				parentNode.appendChild(childNode);
			}
		}
		return doc;
	}
}
