# Check that assignments to an object's members don't affect members of the 
# class object

class A:
	def __init__(self):
		self.i = []

print A.i # what_am_i
