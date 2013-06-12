# The global should have the same type {int, str} everywhere

def f():
	print G # what_am_i_without_global_statement

def g():
	global G
	G = 7
	print G # what_am_i_with_global_statement

f()
g()

G = "Hello"

print G # what_am_i_at_global_scope