# The function is called through an alias as well as through the name introduced
# by its declaration.

def f(x):
	print x # what_am_i
	
G = f

f("Hello")

G(42)