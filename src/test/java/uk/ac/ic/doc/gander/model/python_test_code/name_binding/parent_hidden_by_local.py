# A variable that refers to its local binding despite the same name
# being defined in its enclosing scope as well as the global namespace
i = 42

def f():
	i = "Hello"
	def g():
		
		i = []
		print i # what_am_i
	
	g()

f()