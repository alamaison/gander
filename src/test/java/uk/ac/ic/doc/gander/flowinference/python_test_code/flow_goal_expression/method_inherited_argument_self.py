class A:
	def m(self, a):
		
		print "I am self in A::m"
		print self # self in A::m
		
		print "I am a in A::m"
		print a # not a in A::m

class B(A):
	pass

b = B()
print b # blastoff
b.m(2)
