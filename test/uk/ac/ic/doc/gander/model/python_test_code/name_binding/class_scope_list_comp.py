# A generator expression occurs in its own code block so the use of 'i' in
# the expression does not bind to the class's definition.  Instead it binds to
# the first non-class scope.

i = 42

class A:
	i = "Hello"
	
	# prints [42, 42, 42, 42]
	print list(i for _ in range(1,5))
