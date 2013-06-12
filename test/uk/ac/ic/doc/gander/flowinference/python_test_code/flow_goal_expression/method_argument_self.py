class A:
	def m(self, p):
		print self # self in A::m
		print p # not p in A::m
	
	def n(self, p):
		print self # self in A::n
		print p # and, sneakily, p in A::n 

class B:
	def m(self, p):
		print self # not self in B
		print p # nor p in B

a = A()
print a # blastoff
a.m(2)

a.n(a)
