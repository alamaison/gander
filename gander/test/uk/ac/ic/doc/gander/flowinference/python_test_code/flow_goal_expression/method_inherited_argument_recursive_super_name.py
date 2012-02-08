# A flow-insensitive analysis of this program makes it seem that the 2nd class
# definition might inherit from itself.  Obviously this is not the case, but
# the analysis needs to be protected against this.

class A:
	def m(self, a):
		
		print "I am self in A::m"
		print self # not self in A::m
		
		print "I am a in A::m"
		print a # a in A::m

old_A = A

class A(old_A):
	def n(self, a):
		print "About to call the superclass method"
		self.m(a)

x = 2
print x # blastoff
A().n(x)
