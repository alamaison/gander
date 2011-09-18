# binds a variable in a method to the *nearest* non-class enclosing scope
# despite both classes defining a variable of the same name
i = 42
class A:
	i = "Hello"
	
	class B:
		i = []
		
		def f(self):
			print i # what_am_i_in_a_method
		
		print i # what_am_i_in_the_parent_class
	
	def g(self):
		self.B().f()
	
	print i # what_am_i_in_the_grandparent_class

a = A()
a.g()
