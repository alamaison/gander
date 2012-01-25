# f is called through the parameter of another function.

def f(x):
	print x # x is not f
	return "I am f"

def call_callable(a):
	print a # only this parameter is f
	print a([]) # this is the result of f but is not f
	return "I call f but I am not f"

print call_callable # this is also not f
print call_callable(f) # this passes f but isn't f
