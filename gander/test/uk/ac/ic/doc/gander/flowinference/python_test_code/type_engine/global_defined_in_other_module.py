# First half of the cross-module global test.
# This module's global is assigned in the _worker sister module that import's G
# from this one.
#
# The global should be inferred everywhere as the union of the types assigned
# to it in both modules, namely {int, str, Bob, list} 


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