class A:
	def m(self):
		pass


class B(A):
    def m(self):
    	pass

class C(B):
	
    def __init__(self):
    	print "I inherit m from my parent"

class D(C):
	
    def __init__(self):
    	print "I inherit m from my grandparent"

a = A()

print a.m # what_am_i_overridden

b = B()

print b.m # what_am_i

c = C()

print c.m # what_am_i_parent

d = D()

print d.m # what_am_i_grandparent
