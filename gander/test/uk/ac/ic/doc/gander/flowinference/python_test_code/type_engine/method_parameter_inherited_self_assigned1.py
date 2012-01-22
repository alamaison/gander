class A:
	pass

def m(self):
	print self # what_am_i

A.m = m

class B(A):
	pass

B().m()
