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

def main():
	if True:
		x = A()
	else:
		x = B()
	x.a("tag1") # A B C
	x.b("tag2") # A B C
	
	if True:
		x.c("tag3") # A