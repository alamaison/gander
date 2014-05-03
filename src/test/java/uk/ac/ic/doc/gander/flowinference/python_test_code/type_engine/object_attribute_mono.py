# Object attributes take the type of all writes to the attribute regardless of
# where they occur

class A:
	def __init__(self):
		self.i = []
	
	def f(self):
		print self.i # what_am_i_inside
	
	def g(self):
		self.i = [0]

a = A()
a.f()
a.g()

print a.i # what_am_i_outside
