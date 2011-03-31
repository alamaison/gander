package uk.ac.ic.doc.gander;

import static org.junit.Assert.*;

import org.junit.Test;
import org.python.pydev.parser.jython.FastCharStream;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Pass;

import uk.ac.ic.doc.gander.Parser;

public class ParserTest {
	
	static String INPUT1 = "class Bob: pass";
	static String INPUT2 = "def fun(a):\n\ta.m()";
	static String INPUT3 = "import os\nos.getcwd()";
	static String INPUT4 = "def fun(a):\n\tpass\nfun(1)";
	static String INPUT5 = "class Bob:\n\tdef fun(self, a):\n\t\tpass\n" +
			"b = Bob()\nb.fun(1)";
	
	private SimpleNode doParse(String input) throws Throwable {
		Parser parser = new Parser();
		return parser.parse(new FastCharStream(input.toCharArray()));
	}
	
	@Test
	/** Class definition. */
	public void testParse1() throws Throwable {
		SimpleNode root = doParse(INPUT1);
		Module module = (Module)root;
		ClassDef classBob = (ClassDef)module.body[0];
		assertEquals("Bob", ((NameTok)(classBob.name)).id);
		assertTrue(classBob.body[0] instanceof Pass);
	}

	@Test
	/** Free-function definition. */
	public void testParse2() throws Throwable {
		SimpleNode root = doParse(INPUT2);
		Module module = (Module)root;
		FunctionDef defFun = (FunctionDef)module.body[0];
		assertEquals("fun", ((NameTok)(defFun.name)).id);
		Expr expr = (Expr)defFun.body[0];
		Call call = (Call)expr.value;
		Name name = ((Name)((Attribute)call.func).value);
		assertEquals("a", name.id);		
	}
	
	@Test
	/** Call from import (looks like method call). */
	public void testParse3() throws Throwable {
		SimpleNode root = doParse(INPUT3);
		Module module = (Module)root;
		
		Import importOs = (Import)module.body[0];
		assertEquals("os", ((NameTok)importOs.names[0].name).id);
		
		Expr expr = (Expr)module.body[1];
		Call call = (Call)expr.value;
		Name name = ((Name)((Attribute)call.func).value);
		assertEquals("os", name.id);		
	}
	
	@Test
	/** Call free-function. */
	public void testParse4() throws Throwable {
		SimpleNode root = doParse(INPUT4);
		Module module = (Module)root;
		
		FunctionDef defFun = (FunctionDef)module.body[0];
		assertEquals("fun", ((NameTok)(defFun.name)).id);
		
		Expr expr = (Expr)module.body[1];
		Call call = (Call)expr.value;
		Name name = (Name)call.func;
		assertEquals("fun", name.id);		
	}
	
	@Test
	/** Call method (looks like calling imported function). */
	public void testParse5() throws Throwable {
		SimpleNode root = doParse(INPUT5);
		Module module = (Module)root;
		
		// Assignment to b
		Assign bAssigned = (Assign)module.body[1];
		assertEquals("b", ((Name)bAssigned.targets[0]).id);
		
		// Constructor call: Bob()
		Call call = (Call)bAssigned.value;
		Name name = (Name)call.func;
		assertEquals("Bob", name.id);
		
		// Method call: b.fun(1)
		call = (Call)((Expr)module.body[2]).value;
		name = ((Name)((Attribute)call.func).value);
		assertEquals("b", name.id);
		NameTok name2 = ((NameTok)((Attribute)call.func).attr);
		assertEquals("fun", name2.id);
	}
}
