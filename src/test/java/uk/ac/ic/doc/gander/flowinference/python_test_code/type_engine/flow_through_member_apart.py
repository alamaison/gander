class A:
	def m(self, y):
		print y # what_am_i

class B:
	def __init__(self):
		self.x = A()
	
	def n(self):
		self.x.m(42)

B().n()
