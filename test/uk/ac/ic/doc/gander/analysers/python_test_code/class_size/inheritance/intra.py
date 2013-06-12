class A: # size 3
	def x(self):
		pass
	
	def y(self):
		pass
	
	def z(self):
		pass
	
class B(A): # size 4 (2 + 3 inherited - 1 overridden)
	def p(self):
		pass

	def z(self):
		pass