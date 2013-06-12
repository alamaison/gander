# The self variable in the classes should be inferred monomorphically (i.e. not
# be distracted by the other class) but the outside use should be polymorphic

class A:
	def __init__(self):
		self.i = []
	
	def f(self):
		print self.i # what_am_i_inside
	
	def g(self):
		self.i = [0]
		
class B:
	def __init__(self):
		self.i = "Hello"
	
	def f(self):
		print self.i
	
	def g(self):
		pass

a = A()
if False:
	a = B()
a.f()
a.g()

print a.i # what_am_i_outside
