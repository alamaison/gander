# f is called as the result of another call, once directly and once via
# an alias, g.  All three parameter types should be included in the inferred
# type of x

def f(x):
	print x # what_am_i

f("Hello")

def get_callable(a):
	return f

get_callable([])(42) # should include this int
get_callable({}) # should not include this dict

g = get_callable("")
g([]) # should include this list
