class A:
	def m(self, a):
		print a # what_am_i
	def n(self):
		self.m(999)

A().m(42)
a = A()
a.m(0)