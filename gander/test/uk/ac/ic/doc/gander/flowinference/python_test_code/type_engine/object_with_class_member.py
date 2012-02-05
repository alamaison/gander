class B:
	class A:
		def __init__(self, a):
			print "B::A::__init__::self"
			print self # self in B::A
			
			print "B::A::__init__::a"
			print a # a in B::A

b = B()
a = b.A(2)
