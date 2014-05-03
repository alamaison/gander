# f is called through the parameter of another function.

def f(x):
	print x
	return "I am f"

def call_callable(a):
	print a([]) # this calls f
	return "I call f but I am not f"

print call_callable(f) # this doesn't call f
