class A:
	pass

def m(self, a):
	print a # what_am_i

A.m = m

class B(A):
	pass

B().m([])
