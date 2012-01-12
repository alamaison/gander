# First half of the cross-module global test.
# This module's global is assigned in the _worker sister module that import's G
# from this one but that module's G is just a name.  Assignments to that name
# don't affect what this modules 'G' name points to
#
# The global should be inferred here as the union of _local_ assignments, 
# namely {int, str} 

def g():
	global G
	G = 7

g()

G = "Hello"

print G # what_am_i_before_import
import global_redefined_in_other_module_worker
print G # what_am_i_after_import