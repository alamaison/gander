class A:
	pass

def m(self):
	print self # what_am_i

A.m = m
A().m()
