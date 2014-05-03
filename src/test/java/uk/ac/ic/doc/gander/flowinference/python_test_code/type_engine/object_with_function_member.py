# Tests that an object, whose plain-old function is executed from an attribute
# on the object, isn't flowed into the function

def f(a):
	print "I'm function f"
	print a # what_am_i

class A:
	pass

a = A()
a.g = f

a.g(2)
