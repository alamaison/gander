# f is called via an alias that was put into an object in its constructor.
# This flow should be tracked and both types of argument should be included 
# in the type of x


def f(x):
	print x # what_am_i

f("Hello")

class A:
	def __init__(self, g):
		self.g = f

a = A()

h = a.g
h([]) # inferred type should include this list
