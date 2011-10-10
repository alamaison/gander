# Infer return type of a function taking no arguments and returning a
# polymorph via a nested call.
def f():
	if True:
		return 42
	else:
		class X:
			pass
		
		return X()

def g():
	if True:
		return "Bob"
	else:
		return f()

def h():
	a = g()
	print a # what_am_i