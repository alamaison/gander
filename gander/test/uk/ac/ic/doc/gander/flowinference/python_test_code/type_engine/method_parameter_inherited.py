class A:
	def m(self):
		print self # what_am_i

class B(A):
	pass

a = A()
a.m()

b = B()
b.m()