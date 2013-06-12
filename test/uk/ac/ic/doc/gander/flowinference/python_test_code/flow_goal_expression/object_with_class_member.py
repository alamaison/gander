class A:
	def __init__(self, a):
		print self # self in A
		print a # a in A

class B:
	class A:
		def __init__(self, a):
			print "B::A::__init__::self"
			print self # self in B::A
			
			print "B::A::__init__::a"
			print a # a in B::A

b = B()
print b # blastoff

x = 2
a = b.A(x)
