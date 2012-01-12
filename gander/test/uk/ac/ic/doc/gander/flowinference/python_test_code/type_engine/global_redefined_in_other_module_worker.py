# Second half of the cross-module global test.
# This module assigns to, and uses, a symbol imported from the other module's
# namespace. Because that name is a local copy, assigning to it doesn't
# redfine the name in the module it was imported from

from global_redefined_in_other_module import G

G = []

class Bob():
	pass

def f():
	global G
	G = Bob()

f()

print G