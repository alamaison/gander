# Tests that an object, whose plain-old function is executed from an attribute
# on the object, isn't flowed into the function

def f(a):
	print "I'm function f"
	print a # a in f

class A:
	pass

a = A()
print a # blastoff
a.g = f

x = 2
a.g(x)
