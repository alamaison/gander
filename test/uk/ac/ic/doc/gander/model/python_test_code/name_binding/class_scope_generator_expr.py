# Unlike a generator expression, a list comprehension occurs in the 
# enclosing code block so the use of 'i' in the comprehension binds to the
# class body's definition.

i = 42

class A:
	i = "Hello"
	
	print [i for _ in range(1,5)]
