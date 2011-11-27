# f is called via an alias that was put into an object from outside the
# object's class definition. This flow should be tracked and both types of
# argument should be included in the type of x

def f(x):
	print x # what_am_i

f("Hello")

class A:
	pass

a = A()
a.g = f

h = a.g
h([]) # inferred type should include this list
