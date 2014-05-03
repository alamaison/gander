# Class scopes are special in that the scope of their variables doesn't go
# beyond their own code block.  Therefore the nested class body doesn't see the
# enclosing class's definition
i = 42

class A:
	i = "Hello"
	
	class B:
		print i # what_am_i