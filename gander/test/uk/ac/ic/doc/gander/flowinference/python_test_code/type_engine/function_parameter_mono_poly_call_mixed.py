# the parameter is monomorphic but the callsite is polymorphic.
# this tests that other types passed to other calls to the function that f
# must share its callsite with don't pollute the inferred type of f's parameter
#
# This variant tests the more complicated situation where the two callables
# are of different types (one bound, one not).

def f(x, y):
	print "Inside f:"
	print x # what_am_i_x
	print y # what_am_i_y

class A:
	def g(self, y, x):
		print "Inside A::g:"
		print x
		print y

if 1 + 1 != 2:
	h = f
else:
	a = A()
	h = a.g

h("Hello", [])