# A variable that refers to its grandparent's binding despite the same name
# being defined in the global namespace
i = 42

def f():
	i = "Hello"
	def g():
		def h():
			print i # what_am_i
		
		h()
	
	g()

f()