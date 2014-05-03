class A:
	pass

def m(self, a):
	print a # what_am_i


class B(A):
	pass

A.m = m

B().m([])
