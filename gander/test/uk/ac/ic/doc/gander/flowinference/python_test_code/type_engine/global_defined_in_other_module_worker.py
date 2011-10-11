# Second half of the cross-module global test.
# This module assigns to, and uses, a symbol in the other module's namespace.
#
# The global should be inferred everywhere as the union of the types assigned
# to it in both modules, namely {int, str, Bob, list} 

from global_defined_in_other_module import G

class Bob():
	pass

def f():
	print G # what_am_i_without_global_statement_in_foreign_module

def g():
	global G
	G = Bob()
	print G # what_am_i_with_global_statement_in_foreign_module


f()
g()

G = []

print G # what_am_i_at_global_scope_in_foreign_module