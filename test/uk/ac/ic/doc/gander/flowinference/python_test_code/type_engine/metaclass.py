# The inferred type of the class but be different from the inferred type of
# its instances

class W:
	pass

def fun():
	print W # am_i_a_class
	
	a = W
	print a # am_i_also_a_class
	
	b = W()
	print b # am_i_an_instance
	
	c = a()
	print c # am_i_also_an_instance