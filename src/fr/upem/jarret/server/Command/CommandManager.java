package fr.upem.jarret.server.Command;

import java.util.HashMap;
import java.util.Optional;

public class CommandManager {

	private final HashMap<String,CommandVisitor> commands = new HashMap<>();
	
	public void register(String string, CommandVisitor command){
		commands.put(string, command);
	}
	
	public Optional<CommandVisitor> get(String string){
		return Optional.ofNullable(commands.get(string));
	}
}
