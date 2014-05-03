# A method can be packaged into a closure and called elsewhere.  This makes
# it hard to infer parameter types correctly.

class A:
	def m(self, a):
		print a # what_am_i

a = A()
a.m("Hello")

b = a.m
b(42)
