package de.fuberlin.optimierung;

import java.util.*;

import de.fuberlin.optimierung.commands.*;

/**
 * @author kargerb
 *
 */
/**
 * @author kargerb
 *
 */
class LLVM_Block implements ILLVM_Block {
	
	// Funktion, zu der der Block gehoert
	private LLVM_Function function = null;
	
	// Erster und letzter Befehl des Blockes
	private ILLVM_Command firstCommand = null;
	private ILLVM_Command lastCommand = null;

	// Ursprüngliches Label des Blockes
	private String label = "";
	
	// Vorgaenger- und Nachfolgerbloecke
	// Hieraus entsteht der Flussgraph zwischen den Bloecken
	private LinkedList<ILLVM_Block> nextBlocks = new LinkedList<ILLVM_Block>();
	private LinkedList<ILLVM_Block> previousBlocks = new LinkedList<ILLVM_Block>();
	
	// def- und usemenge an Speicheradressen fuer globale Lebendigkeitsanalyse
	private LinkedList<String> def = new LinkedList<String>();
	private LinkedList<String> use = new LinkedList<String>();
	// IN und OUT Mengen fuer globale Lebendigkeitsanalyse auf Speicherzellen
	private LinkedList<String> inLive = new LinkedList<String>();
	private LinkedList<String> outLive = new LinkedList<String>();
	
	// Kompletter Code des Blocks als String
	private String blockCode;
	
	HashMap<String, LinkedList<ILLVM_Command>> commonex = new HashMap<String, LinkedList<ILLVM_Command>>();

	public LLVM_Block(String blockCode, LLVM_Function function) {
		
		this.function = function;
		this.blockCode = blockCode;
		System.out.println(blockCode + "\n*****************\n");
		
		this.createCommands();
		this.optimizeBlock();
	}

	public void optimizeBlock() {
	}
	
	/**
	 * Löscht alle doppelten Befehle in einem Block
	 * Bei Änderungen wird ConstantPropagation aufgerufen
	 * Doppelte Befehle werden nur überprüft, falls in Whitelist
	 */
	public void removeCommonExpressions() {
		List<String> whitelist = new ArrayList<String>();
		LinkedList<ILLVM_Command> changed = new LinkedList<ILLVM_Command>();
		whitelist.add(LLVM_Operation.ADD.toString());
		whitelist.add(LLVM_Operation.MUL.toString());
		whitelist.add(LLVM_Operation.DIV.toString());
		whitelist.add(LLVM_Operation.SUB.toString());
		
		for (ILLVM_Command i = this.firstCommand; i != null; i=i.getSuccessor()){
			// Nur Kommandos aus der Whitelist optimieren
			if (!whitelist.contains(i.getOperation().name())) continue;
			
			if (commonex.containsKey(i.getOperation().name())){
				// Kommando-Hash existiert
				boolean matched = false;
				LinkedList<ILLVM_Command> commands = commonex.get(i.getOperation().name());
				for (ILLVM_Command command : commands){
					if (matchCommands(i, command)){
						// gleiches Kommando gefunden
						// ersetze aktuelles Kommando mit Bestehendem
						matched = true;
						System.out.println("same command at " + command.getTarget().getName() + ", command replaced : " + i.toString());
						this.function.getRegisterMap().deleteCommand(i);
						i.setOperation(LLVM_Operation.ADD);
						i.getOperands().get(0).setName(command.getTarget().getName());
						i.getOperands().get(1).setName("0");
						this.function.getRegisterMap().addCommand(i);
						changed.add(i);
					}
				}
				if (!matched){
					// Kein übereinstimmendes Kommando gefunden
					// füge aktuelles Kommando zum Kommando-Hash hinzu
					commands.add(i);
					commonex.put(i.getOperation().name(), commands);
				}
			}
			else{
				// Kommando-Hash existiert nicht
				LinkedList<ILLVM_Command> tmp = new LinkedList<ILLVM_Command>();
				tmp.add(i);
				commonex.put(i.getOperation().name(), tmp);
			}
		}
		this.function.constantPropagation(changed);
	}
	
	private boolean matchCommands(ILLVM_Command com1, ILLVM_Command com2){
		int i = 0;
		// Gleichviele Parameter?
		if (com1.getOperands().size() != com2.getOperands().size()) return false;
		// Gleiche Operation?
		if (com1.getOperation() != com2.getOperation()) return false;
		// Gleiche Parameter?
		for (LLVM_Parameter para1 : com1.getOperands()){
			LLVM_Parameter para2 = com2.getOperands().get(i);
			if (para1.getType() == para2.getType() && para1.getName().equals(para2.getName())){
				// Nothing
			}else{
				// Parameter matchen nicht
				return false;
			}
			i++;
		}
		return true;
	}
	
	/*
	 * *********************************************************
	 * *********** Live Variable Analysis **********************
	 * *********************************************************
	 */
	
	/**
	 * Entferne ueberfluessige Stores
	 * Vorraussetzung: IN und OUT mengen der globalen lebendigkeitsanalyse sind gesetzt
	 */
	public void deleteDeadStores() {
		LinkedList<String> active = (LinkedList<String>) this.outLive.clone();
		LinkedList<ILLVM_Command> deletedCommands = new LinkedList<ILLVM_Command>();
		
		// Gehe Befehle von hinten durch
		ILLVM_Command c = this.lastCommand;
		for(;c!=null; c = c.getPredecessor()) {
			if(c.getOperation()==LLVM_Operation.STORE) {
				if(!active.contains(c.getOperands().get(1).getName())) {
					// c kann geloescht werden
					this.function.getRegisterMap().deleteCommand(c);
					c.deleteCommand();
					deletedCommands.add(c);
				}
				else {
					// jetzt ist es nicht mehr aktiv
					active.remove(c.getOperands().get(1).getName());
				}
			}
			if(c.getOperation()==LLVM_Operation.LOAD) {
				active.add(c.getOperands().getFirst().getName());
			}
		}
		
		// Teste, ob geloeschter Befehl Operanden hatte, der nun keine Verwendung mehr hat
		// Dann kann die Definition entfernt werden
		while(!deletedCommands.isEmpty()) {
			
			deletedCommands = this.function.eliminateDeadRegistersFromList(deletedCommands);
			
		}
	}
	
	/**
	 * Erstelle def und use Mengen dieses Blockes fuer globale Lebendigkeitsanalyse
	 */
	public void createDefUseSets() {
		if(!this.isEmpty()) {
			ILLVM_Command c = this.firstCommand;
			while(c!=null) {
				if(LLVM_Operation.STORE==c.getOperation()) {
					// Register mit Speicheradresse steht in zweitem Operanden
					LLVM_Parameter p = c.getOperands().getLast();
					String registerName = p.getName();
					
					// registerName muss in this.def, falls es keine vorherige Verwendung
					// gab, also falls registerName nicht in this.use enthalten ist
					if(!this.use.contains(registerName)) {
						this.def.add(registerName);
					}
				}
				else if(LLVM_Operation.LOAD==c.getOperation()) {
					// Register mit Speicheradresse steht in erstem Operanden
					LLVM_Parameter p = c.getOperands().getFirst();
					String registerName = p.getName();
					
					// registerName muss in this.use, falls es keine vorherige Definition
					// gab, also falls registerName nicht in this.def enthalten ist
					if(!this.def.contains(registerName)) {
						this.use.add(registerName);
					}
				}
				c = c.getSuccessor();
			}
		}
		
	}
	
	/**
	 * Aktualisiere IN und OUT Mengen fuer globale Lebendigkeitsanalyse
	 * Voraussetzung: def und use sind gesetzt
	 * @return true, falls IN veraendert wurde
	 */
	public boolean updateInOutLiveVariables() {
		
		// this.out = in-Mengen aller Nachfolger zusammenfuegen
		this.outLive.clear();
		for(ILLVM_Block b : this.nextBlocks) {
			LinkedList<String> inNextBlock = b.getInLive();
			for(String s : inNextBlock) {
				if(!this.outLive.contains(s)) {
					this.outLive.add(s);
				}
			}
		}
		
		// this.in = this.use + (this.out - this.def)
		//this.inLive.clear();
		LinkedList<String> inLiveOld = this.inLive;
		this.inLive = (LinkedList<String>) this.outLive.clone();	// gibt doch neues obj zurueck?
		for(String s : this.def) {
			this.inLive.remove(s);
		}
		for(String s : this.use) {
			if(!this.inLive.contains(s)) {
				this.inLive.add(s);
			}
		}
		
		return !(this.compareLists(inLiveOld, this.inLive));
				
	}
	
	/*
	 * *********************************************************
	 * *********** Umgang mit Befehlen *************************
	 * *********************************************************
	 */

	private boolean labelCheck(String label) {
		
		if(label.charAt(0) == ';') {
			//String[] splitedLabel = label.split("[:;]");
			//this.label = "%"+splitedLabel[2].trim();
			//this.label_line = label;
			return true;
		}else{
			String[] splitedLabel = label.split(":");
			
			if(splitedLabel.length >= 2){
				this.label = "%"+splitedLabel[0];
				//this.label_line = label;
				return true;
			}
		}
		
		return false;
	}
	
	private void createCommands() {
		String commandsArray[] = this.blockCode.split("\n");
		
		int i = 0;
		
		if(commandsArray[0].length() == 0){
			i++;
		}
		
		// Checking for label
		if(labelCheck(commandsArray[i])){
			i++;
		}
		
		
		this.firstCommand = mapCommands(commandsArray[i], null);
		
		ILLVM_Command predecessor = firstCommand;
		for(i++; i<commandsArray.length; i++) {
			ILLVM_Command c = mapCommands(commandsArray[i], predecessor);
			if(firstCommand == null){
				firstCommand = c;
				predecessor = c;
			}else{
				predecessor = c;
			}
		}
		this.lastCommand = predecessor;
	}
	
	// Ermittelt Operation und erzeugt Command mit passender Klasse
	//TODO elegante Methode finden, switch funktioniert auf Strings nicht!
	private LLVM_GenericCommand mapCommands(String cmdLine, ILLVM_Command predecessor){
		
		// Kommentar Handling
		if (cmdLine.trim().startsWith(";")){
			return new LLVM_Comment(null, LLVM_Operation.COMMENT, predecessor, this, cmdLine.replaceFirst(";", "").trim());
		}
		
		String[] com = cmdLine.trim().split(";");
		String comment = "";
		
		if (com.length > 1){
			for (int i = 1; i < com.length; i++){
				comment += com[i]; 
			}
		}
		
		if (com.length == 0) return null;
		
		// Kommando Handling
		String[] cmd = com[0].trim().split("[ \t]");
		
		if (cmd.length > 0){
			if (cmd[0].compareTo("br") == 0){
				if (cmd[1].compareTo("label") == 0){
					return new LLVM_BranchCommand(cmd, LLVM_Operation.BR, predecessor, this, comment);
				}else{
					return new LLVM_BranchCommand(cmd, LLVM_Operation.BR_CON, predecessor, this, comment);
				}
			} else if (cmd[0].compareTo("ret") == 0){
				if (cmd[1].compareTo("void") == 0){
					return new LLVM_ReturnCommand(cmd, LLVM_Operation.RET, predecessor, this, comment);
				}else{
					return new LLVM_ReturnCommand(cmd, LLVM_Operation.RET_CODE, predecessor, this, comment);
				}
			} else if (cmd[0].compareTo("store") == 0){
				return new LLVM_StoreCommand(cmd, LLVM_Operation.STORE, predecessor, this, comment);
			}
			if (cmd.length > 3 && cmd[1].equals("=")){
				
				if (cmd[2].compareTo("add") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.ADD, predecessor, this, comment);
				}else if(cmd[2].compareTo("sub") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.SUB, predecessor, this, comment);
				}else if(cmd[2].compareTo("mul") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.MUL, predecessor, this, comment);
				}else if(cmd[2].compareTo("div") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.DIV, predecessor, this, comment);
				}else if(cmd[2].compareTo("urem") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.UREM, predecessor, this, comment);
				}else if(cmd[2].compareTo("srem") == 0){
					return new LLVM_ArithmeticCommand(cmd, LLVM_Operation.SREM, predecessor, this, comment);
				}else if (cmd[2].compareTo("alloca") == 0){
					return new LLVM_AllocaCommand(cmd, LLVM_Operation.ALLOCA, predecessor, this, comment);
				}else if (cmd[2].compareTo("and") == 0){
					return new LLVM_LogicCommand(cmd, LLVM_Operation.AND, predecessor, this, comment);
				}else if (cmd[2].compareTo("or") == 0){
					return new LLVM_LogicCommand(cmd, LLVM_Operation.OR, predecessor, this, comment);
				}else if (cmd[2].compareTo("xor") == 0){
					return new LLVM_LogicCommand(cmd, LLVM_Operation.XOR, predecessor, this, comment);
				}else if (cmd[2].compareTo("load") == 0){
					return new LLVM_LoadCommand(cmd, LLVM_Operation.LOAD, predecessor, this, comment);
				}else if (cmd[2].compareTo("call") == 0 || cmd[3].compareTo("call") == 0){
					return new LLVM_CallCommand(cmd, LLVM_Operation.CALL, predecessor, this, comment);
				}else if (cmd[2].compareTo("icmp") == 0){
					if (cmd[3].compareTo("eq") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_EQ, predecessor, this, comment);
					}else if (cmd[3].compareTo("ne") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_NE, predecessor, this, comment);
					}else if (cmd[3].compareTo("ugt") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_UGT, predecessor, this, comment);
					}else if (cmd[3].compareTo("uge") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_UGE, predecessor, this, comment);
					}else if (cmd[3].compareTo("ult") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_ULT, predecessor, this, comment);
					}else if (cmd[3].compareTo("ule") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_ULE, predecessor, this, comment);
					}else if (cmd[3].compareTo("sgt") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_SGT, predecessor, this, comment);
					}else if (cmd[3].compareTo("sge") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_SGE, predecessor, this, comment);
					}else if (cmd[3].compareTo("slt") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_SLT, predecessor, this, comment);
					}else if (cmd[3].compareTo("sle") == 0){
						return new LLVM_IcmpCommand(cmd, LLVM_Operation.ICMP_SLE, predecessor, this, comment);
					}
				}
			}
		}
		return null;
	}
	
	/*
	 * *********************************************************
	 * *********** Hilfsfunktionen *****************************
	 * *********************************************************
	 */

	/**
	 * Hilfsfunktion, um zwei String-Listen zu vergleichen
	 * Gibt true zurueck, wenn sie die gleichen Strings enthalten (Reihenfolge egal),
	 * sonst false
	 * @param l1 Liste 1
	 * @param l2 Liste 2
	 * @return
	 */
	private boolean compareLists(LinkedList<String> l1, LinkedList<String> l2) {
		if(l1.size()!=l2.size()) {
			return false;
		}
		for(String s : l1) {
			if(!l2.contains(s)) {
				return false;
			}
		}
		return true;
	}
	
	public void deleteBlock() {

		for(ILLVM_Block nextBlock : this.nextBlocks) {
			nextBlock.removeFromPreviousBlocks(this);
		}
		
	}
	
	public boolean isEmpty() {
		return (this.firstCommand==null);
	}
	
	public boolean hasPreviousBlocks() {
		return !(this.previousBlocks.isEmpty());
	}
	
	/*
	 * *********************************************************
	 * *********** Setter / Getter / toString ******************
	 * *********************************************************
	 */
	
	public void setFirstCommand(ILLVM_Command first) {
		this.firstCommand = first;
	}

	public void setLastCommand(ILLVM_Command last) {
		this.lastCommand = last;
	}
	
	public ILLVM_Command getFirstCommand() {
		return firstCommand;
	}

	public ILLVM_Command getLastCommand() {
		return lastCommand;
	}
	
	public LinkedList<ILLVM_Block> getNextBlocks() {
		return nextBlocks;
	}

	public void appendToNextBlocks(ILLVM_Block block) {
		this.nextBlocks.add(block);
	}
	
	public void removeFromNextBlocks(ILLVM_Block block) {
		this.nextBlocks.remove(block);
	}

	public LinkedList<ILLVM_Block> getPreviousBlocks() {
		return previousBlocks;
	}

	public void appendToPreviousBlocks(ILLVM_Block block) {
		this.previousBlocks.add(block);
	}
	
	public void removeFromPreviousBlocks(ILLVM_Block block) {
		this.previousBlocks.remove(block);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public LinkedList<String> getInLive() {
		return inLive;
	}

	public String toString() {
		
		String code = "";
		
		if(!this.label.matches("%[1-9][0-9]*") && !this.label.equals("")) {
			code = label.substring(1)+":\n";
		}
		
		ILLVM_Command tmp = firstCommand;
		while(tmp != null){
			code += "\t"+tmp.toString();
			tmp = tmp.getSuccessor();
		}
		code += "\n";
		
		return code;
	}
	
	public String toGraph() {
		String graph = "\""+label+"\" [ style = \"filled, bold\" penwidth = 5 fillcolor = \"white\" fontname = \"Courier New\" shape = \"Mrecord\" label =<<table border=\"0\" cellborder=\"0\" cellpadding=\"3\" bgcolor=\"white\"><tr><td bgcolor=\"black\" align=\"center\" colspan=\"2\"><font color=\"white\">"+label+"</font></td></tr>";
		
		ILLVM_Command tmp = firstCommand;
		while(tmp != null){
			graph += "<tr><td align=\"left\">"+ tmp.toString() +"</td></tr>";
			tmp = tmp.getSuccessor();
		}
		
		return graph + "</table>> ];";
	}
}
