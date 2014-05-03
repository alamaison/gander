# Infer return type of a function taking no arguments and returning a monomorph.
def f():
	return 42

def g():
	a = f()
	print a # what_am_i