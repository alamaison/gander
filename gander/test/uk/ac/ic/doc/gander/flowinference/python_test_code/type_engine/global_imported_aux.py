# The global should have the same type {int, str} everywhere

def g():
	global G
	G = 7
	print G

g()

G = "Hello"