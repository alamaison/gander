class A:
	def m(self, a):
		print a # what_am_i
	def n(self):
		self.m([])

A().m(42)
a = A()
a.m("Hello")