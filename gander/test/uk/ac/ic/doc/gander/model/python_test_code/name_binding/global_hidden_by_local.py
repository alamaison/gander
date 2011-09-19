# A variable that refers to its local binding despite the same name being
# defined in the global namespace
i = 42

def f():
	i = "Hello"
	print i # what_am_i

f()