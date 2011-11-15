# Check that assignments to self in the constructor register

class A:
	def __init__(self):
		self.i = []

print A().i # what_am_i
