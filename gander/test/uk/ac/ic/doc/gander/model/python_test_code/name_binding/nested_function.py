# Bindings in a nested scope can't affect the value of the outer scope except
# globals

def x():
	a = 42
	def y():
		a = "Hello"
		print a # who_am_i_inside
	
	y()
	print a # who_am_i_outside

x()
