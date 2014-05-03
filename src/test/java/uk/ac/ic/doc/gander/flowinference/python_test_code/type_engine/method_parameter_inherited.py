class A:
	def m(self, a):
		print a # what_am_i

class B(A):
	pass

a = A()
a.m("Hello")

b = B()
b.m({})