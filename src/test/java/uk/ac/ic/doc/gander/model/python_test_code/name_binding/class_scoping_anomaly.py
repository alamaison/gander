# binds a variable in a method to the scope *outside* its defining class
# despite its class defining a variable of the same name
i = 42
class A:
	i = "Hello"
	
	def f(self):
		print i # what_am_i_in_a_method
	
	print i # what_am_i_in_the_class

a = A()
a.f()
