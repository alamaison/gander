# Object attributes also need to take into account class attributes with the
# same name because lookup falls back to the classes dictionary if looking
# in the object's fails to find anything

class A:
	i = "Hello"
	
	def __init__(self):
		self.i = []
	
	def f(self):
		i = 99
		print self.i # i_am_really_the_instance_var
	
	def g(self):
		del self.i
		print self.i # i_am_really_the_class_var

a = A()
a.f()
a.g()