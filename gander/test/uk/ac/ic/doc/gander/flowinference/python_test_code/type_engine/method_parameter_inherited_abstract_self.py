# The 'self' in the so-called Abstract class is never an instance of the
# Abstract class

class Abstract:
	def m(self):
		print self # what_am_i

class Concrete(Abstract):
	pass

x = Concrete()
x.m()