# Tests that a plain-old function executed from an attribute isn't treated as
# a method

def f(a):
	print "I'm function f"
	print a # a in f

def g(a):
	print "I'm function g"
	print a # not a in g

class A:
	def f(self):
		print "I'm method A::f"
		print self # this method is a distraction

a = A()
a.g = f

x = 2
print x # blastoff

a.g(x)
