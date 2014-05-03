# Infer return type of a function taking no arguments.
#
# Flow/context insensitive analysis will infer the return type as a union
# of int and X even though the latter is unreachable.
def f():
	if True:
		return 42
	else:
		class X:
			pass
		
		return X()

def g():
	a = f()
	print a # what_am_i