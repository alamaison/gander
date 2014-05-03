# The binding in the nested scope shouldn't block the outer use of a resolving
# to the global scope

a = 42

def x():
	def y():
		a = "Hello"
		print a # who_am_i_inside
	
	y()
	print a # who_am_i_outside

x()
