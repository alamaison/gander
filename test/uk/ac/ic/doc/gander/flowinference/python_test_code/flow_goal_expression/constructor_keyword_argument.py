class A:
	def __init__(self, a):
		print a # a in A

def f(self, a):
	print a # also a in f

A.__init__ = f

class B:
	def __init__(self, a, b):
		print a # a in B
		print b # not b in B

class C:
	def __init__(self, a, b):
		print b # b in C
		print a # nor a in C

x = 2
print x # blastoff

A(a=x)

y = 3
B(b=y, a=x)
C(y, b=x)
