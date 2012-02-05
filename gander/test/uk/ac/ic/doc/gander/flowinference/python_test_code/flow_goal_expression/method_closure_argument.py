class A:
	def m(self, a):
		print a # a in A::m
		print self # not self in A::m

class B:
	def m(self, a):
		print a # nor a in B
		print self # nor self in B

x = 2
print x # blastoff
c = A().m
c(x)
