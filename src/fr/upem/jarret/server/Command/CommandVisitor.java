package fr.upem.jarret.server.Command;

import fr.upem.jarret.server.JarretServer;

public interface CommandVisitor {
	void visit(JarretServer server);
}
