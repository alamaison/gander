# A variable that is bound to the local name despite the presence of the global
# keyword in a nested scope

i = 42

def f():
	i = "Hello"
	def g():
		def h(): # the global declaration in the child scope does not affect g
			global i
		
		h()
		print i # what_am_i
	
	g()

f()