# The use of 'i' in the lambda binds to the outer scope because its body
# is not part of the class's code block and the scope of class' local
# variables are limited to their own code blocks

i = 42

class A:
	i = "Hello"
	
	a = lambda x : i
	print a(2)
