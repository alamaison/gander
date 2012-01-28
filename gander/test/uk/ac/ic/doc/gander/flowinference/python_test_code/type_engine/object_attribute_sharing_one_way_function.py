# check that sharing of object and class members only flows one way: objects
# share class member but not vice versa

class A:
	def f(self):
		pass

def g(x):
	pass

a = A()
a.f = g

print A.f # what_am_i_class

print a.f # what_am_i_instance

