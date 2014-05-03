class A:
	def __init__(self, a):
		print a # what_am_i

class B(A):
	def __init__(self):
		A.__init__(self, 42)

B()