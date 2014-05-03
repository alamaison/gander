package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.List;

import org.python.pydev.parser.jython.ParseException;

public interface MutableModel extends Model {

	public abstract Module load(String importName) throws ParseException,
			IOException;

	public abstract Module loadModule(String fullyQualifiedName)
			throws ParseException, IOException;

	public abstract Module loadPackage(String fullyQualifiedName)
			throws ParseException, IOException;

	/**
	 * Load module if it hasn't been loaded before.
	 * 
	 * Will also load any parent packages if they haven't been loaded yet.
	 */
	public abstract Module loadModule(List<String> fullyQualifiedPath)
			throws ParseException, IOException;

	/**
	 * Load package if it hasn't been loaded before.
	 * 
	 * Will also load any parent packages if they haven't been loaded yet.
	 */
	public abstract Module loadPackage(List<String> fullyQualifiedPath)
			throws ParseException, IOException;

}