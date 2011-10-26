# Inferring the types of the methods themselves
class A:
	def method(self):
		pass

m = A.method
m()
print m # what_am_i_via_class

a = A()
n = a.method
n()
print n # what_am_i_via_instance

