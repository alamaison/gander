class A:
	pass

def bob(self):
	print self # what_am_i

A.__init__ = bob
A()