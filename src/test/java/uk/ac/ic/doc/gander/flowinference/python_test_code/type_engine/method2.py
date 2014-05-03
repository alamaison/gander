# This case was causing problems where the call to a.g() flowed the value of
# a into g's self but the list value was then (wrongly) flowing back to the
# method causing they type of a.g to be wrong.

class A:
	
	def g(self):
		self.i = [0]

a = A()
a.g()

print a.g # what_am_i
