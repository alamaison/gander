class I:
	def __init__(self):
		self.virtual()

class S(I):
	def virtual(self):
		print self # what_am_i

S()