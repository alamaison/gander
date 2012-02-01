class A:
	def m(self, a):
		print a # a in A::m
		print self # not self in A::m
	
	def n(self, a):
		print a # not a in A::n
		print self # nor self in A::n

class B:
	def m(self, a):
		print a # nor a in B
		print self # nor self in B

x = 2
print x # blastoff
A().m(x)

y = 3
A().n(y)
