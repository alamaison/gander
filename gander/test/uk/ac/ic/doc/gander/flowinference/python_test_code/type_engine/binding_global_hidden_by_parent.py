# A variable that refers to its parent's binding despite the same name being
# defined in the global namespace
i = 42

def f():
	i = "Hello"
	def g():
		print i # what_am_i
		
	g()

f()