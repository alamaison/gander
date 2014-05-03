# the parameter is monomorphic but the callsite is polymorphic.
# this tests that other types passed to other calls to the function that f
# must share its callsite with don't pollute the inferred type of f's parameter
#
# It is important that f and g share parameter names but at different positions
# as this increases our chances of detecting the case where g is poluting the
# result of typing f's parameters

def f(x, y):
	print x # what_am_i_x
	print y # what_am_i_y

def g(y, x):
	print x

if 1 + 1 == 2:
	h = f
else:
	h = g

h("Hello", [])
g(42, {})