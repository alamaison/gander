# The global should have the same type {int, str} everywhere

from global_imported_aux import G

def f():
	print G # what_am_i_without_global_statement

def g():
	global G
	print G # what_am_i_with_global_statement

f()
g()

print G # what_am_i_at_global_scope
