# f is called through the parameter of another function.
# tests flow through call

def f(x):
	print x # what_am_i

f("Hello")

def call_callable(a):
	a([]) # should include this int

call_callable(f)
