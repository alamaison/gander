# A variable that is bound to the global name despite the same name being 
# defined in an enclosing scope's namespace because of the presence of the
# global keyword in a grandparent scope

i = 42

def f():
	i = "Hello"
	def g():
		global i
		def gg():
			def h(): # although the global is not in h's scope it still affects it
				print i # what_am_i
		
			h()
		
		gg()
	
	g()

f()
