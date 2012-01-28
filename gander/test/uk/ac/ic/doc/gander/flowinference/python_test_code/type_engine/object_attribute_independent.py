# Check that assignments to an object's members don't affect members of the 
# class object

class A:
	pass

A().i = []

print A.i # what_am_i
