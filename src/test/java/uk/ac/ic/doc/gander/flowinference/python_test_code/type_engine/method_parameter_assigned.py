class A:
	pass

def m(self, a):
	print a # what_am_i

A.m = m
A().m(42)
