class A:
	pass

def m(self):
	print self # what_am_i


class B(A):
	pass

A.m = m

B().m()
