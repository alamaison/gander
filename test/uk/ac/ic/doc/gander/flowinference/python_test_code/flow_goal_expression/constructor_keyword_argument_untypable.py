# The constructor has two implementations with different parameters names

class A:
	def __init__(self, a):
		print a # a in A

def f(self, p):
	print p # also p in f

A.__init__ = f

x = 2
print x # blastoff

A(p=x)
