class A:
	def a(self):
		pass
	def b(self):
		pass
	def c(self):
		pass

class B:
	def a(self):
		pass
	def b(self):
		pass

class C:
	def a(self):
		pass
	def b(self):
		pass
	def d(self):
		pass

def get_object():
	if True:
		x = A()
	else:
		x = B()
