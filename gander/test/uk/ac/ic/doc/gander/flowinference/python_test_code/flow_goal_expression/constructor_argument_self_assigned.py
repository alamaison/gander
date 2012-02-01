class A:
	def __init__(self, a):
		print self # self in A
		print a # not a in A

def f(self, p):
	print self # also self in f
	print p # nor p in f

A.__init__ = f

x = 2

print A(x) # blastoff