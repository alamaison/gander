# A variable that is bound to the global name despite the same name being 
# defined in the enclosing scope's namespace because of the presence of the
# global keyword in the local scope

i = 42

def f():
	i = "Hello"
	def g():
		global i
		print i # what_am_i
	
	g()

f()