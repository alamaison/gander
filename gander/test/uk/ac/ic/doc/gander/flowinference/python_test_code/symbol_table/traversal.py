# make sure the symbol table traverses into the ast

if a():
	import gertrude as p
	
# but not too far - if traversal goes into function, 'q' will appear in the
# module's symbol table.  It shouldn't

def x():
	import gertrude as q

class Y:
	import gertrude as r