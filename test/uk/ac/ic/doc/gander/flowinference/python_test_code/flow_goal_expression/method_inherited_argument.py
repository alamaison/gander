class A:
	def m(self, a):
		
		print "I am self in A::m"
		print self # not self in A::m
		
		print "I am a in A::m"
		print a # a in A::m

class B(A):
	pass

x = 2
print x # blastoff
B().m(x)
