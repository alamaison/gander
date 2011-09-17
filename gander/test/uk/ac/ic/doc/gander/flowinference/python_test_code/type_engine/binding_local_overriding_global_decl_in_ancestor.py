# A variable that, despite the ancestral global declaration, is bound locally 
# because it is defined locally.  Outside the scope of this definition the
# global keyword affects nested blocks as usual

i = 42

def f():
	def g():
		global i
		def h(): # the ancestral 'global' has no effect because of the local definition
			i = "Hello"
			print i # what_am_i_locally
		
		h()
		
		def h2(): # the ancestral global effects this scope as usual
			print i # what_am_i_outside
			
		h2()
	
	g()

f()
