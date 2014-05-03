class A:
	def m(self, p):
		print self # self in A::m
		print p # not p in A::m

class B:
	def m(self, p):
		print self # not self in B
		print p # nor p in B

a = A()
print a # blastoff
c = a.m
c(2)
